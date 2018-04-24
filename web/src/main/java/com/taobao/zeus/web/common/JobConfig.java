package com.taobao.zeus.web.common;

public class JobConfig {
    public static final String JAVA_MAIN_KEY="java.main.class";
    public static final String DEPENDENCY_CYCLE="zeus.dependency.cycle";
    public static final String PRIORITY_LEVEL = "run.priority.level";
    public static final String ROLL_TIMES = "roll.back.times";
    public static final String ROLL_INTERVAL = "roll.back.wait.time";
    public static final String ENCRYPTION = "zeus.secret.script";
    public static final String MAX_TIME= "zeus.job.maxtime";
    public static final String POSITIVE_INTEGER = "^[0-9]*[1-9][0-9]*$";
}
