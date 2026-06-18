package com.aliceprotocol.mahosia.mahovm.tweening;

import com.aliceprotocol.mahosia.mahomodel.*;
import com.aliceprotocol.mahosia.mahovm.MahosiaVM;
import javafx.animation.AnimationTimer;
import javafx.collections.*;

public class TweenEngine {

    public static class ActiveTween {
        public final TweenConfig config;
        public final UniformEntry target;
        double elapsed;
        boolean forward = true;

        public ActiveTween(TweenConfig config, UniformEntry target) {
            this.config = config;
            this.target = target;
        }

        @Override
        public String toString() {
            return config.getUniformId() + "[" + config.getComponentIdx() + "] "
                    + config.getStartValue() + "→" + config.getEndValue()
                    + " " + config.getDuration() + "s " + config.getMode();
        }
    }

    private final ObservableList<ActiveTween> activeTweens = FXCollections.observableArrayList();
    private final MahosiaVM vm;
    private final AnimationTimer timer;
    private long lastNano = -1;

    public TweenEngine(MahosiaVM vm) {
        this.vm = vm;
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };
    }

    public void start() { lastNano = -1; timer.start(); }
    public void stop() { timer.stop(); }

    public ObservableList<ActiveTween> getActiveTweens() { return activeTweens; }

    public void addTween(TweenConfig config, UniformEntry target) {
        activeTweens.add(new ActiveTween(config, target));
    }

    public void removeTween(ActiveTween tween) {
        activeTweens.remove(tween);
    }

    private void tick(long nowNano) {
        if (lastNano < 0) { lastNano = nowNano; return; }
        double dt = (nowNano - lastNano) / 1_000_000_000.0;
        lastNano = nowNano;

        var it = activeTweens.iterator();
        while (it.hasNext()) {
            ActiveTween at = it.next();
            at.elapsed += dt;
            TweenConfig c = at.config;
            double dur = c.getDuration();
            float val;

            switch (c.getMode()) {
                case SINGLE: {
                    double t = Math.min(at.elapsed / dur, 1.0);
                    val = lerp(c.getStartValue(), c.getEndValue(), (float) t);
                    if (t >= 1.0) it.remove();
                    break;
                }
                case LOOP: {
                    double t = (at.elapsed % dur) / dur;
                    val = lerp(c.getStartValue(), c.getEndValue(), (float) t);
                    break;
                }
                case PING_PONG: {
                    double cycle = at.elapsed % (dur * 2);
                    double t = cycle <= dur ? cycle / dur : (dur * 2 - cycle) / dur;
                    val = lerp(c.getStartValue(), c.getEndValue(), (float) t);
                    break;
                }
                default: continue;
            }

            int ci = c.getComponentIdx();
            if (ci >= 0) {
                at.target.setValue(ci, val);
            } else {
                for (int i = 0; i < at.target.getType().componentCount; i++) {
                    at.target.setValue(i, val);
                }
            }
            vm.updateUniform(at.target);
        }
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}