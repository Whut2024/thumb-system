package top.liuqiao.thumb.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * 实时记录前 k 个高频访问 key 的堆接口
 * @author liuqiao
 * @since 2025-04-21
 */
public interface TopK {
    /**
     * 给指定的 key 添加权重
     */
    AddResult add(String key, int increment);

    /**
     * 列出堆中有哪些 key 以及 key 对应的权重 <br>
     * 这两个数据被封装为 {@link Item}
     */
    List<Item> list();

    /**
     * 在队列更新过程中被淘汰的 key
     */
    BlockingQueue<Item> expelled();

    /**
     * 削减每个 key 的权重，定时清理
     */
    void fading();

    /**
     * 堆的元素数量
     * @return
     */
    long total();
}