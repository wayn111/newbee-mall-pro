package ltd.newbee.mall.interceptor.threadlocal;

public class AdminLoginThreadLocal {

    private static final ThreadLocal<Integer> ADMIN_THREAD_LOCAL = ThreadLocal.withInitial(() -> 0);

    public static void set(Integer adminUserId) {
        ADMIN_THREAD_LOCAL.set(adminUserId);
    }

    public static Integer get() {
        return ADMIN_THREAD_LOCAL.get();
    }

    public static void remove() {
        ADMIN_THREAD_LOCAL.remove();
    }
}
