package me.gravitinos.aigame.server;

import java.util.concurrent.locks.LockSupport;

public class Scheduler {
    private static class ScheduledTask {
        public final Runnable task;
        public int delayTicks;
        public int repeatTicks = -1;

        public ScheduledTask(Runnable task, int delayTicks) {
            this.task = task;
            this.delayTicks = delayTicks;
        }

        public ScheduledTask(Runnable task, int delayTicks, int repeatTicks) {
            this.task = task;
            this.delayTicks = delayTicks;
            this.repeatTicks = repeatTicks;
        }
    }


}
