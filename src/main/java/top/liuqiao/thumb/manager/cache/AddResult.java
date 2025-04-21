package top.liuqiao.thumb.manager.cache;

import lombok.Data;

/**
 * 添加 k - v 数据进入 heavy keeper {@link HeavyKeeper} 进行热点判断操作的返回结果封装对象
 * @author liuqiao
 * @since 2025-04-21
 */
@Data
public class AddResult {
    // 被挤出的 key
    private final String expelledKey;
    // 当前 key 是否进入 TopK
    private final boolean isHotKey;
    // 当前操作的 key
    private final String currentKey;

    public AddResult(String expelledKey, boolean isHotKey, String currentKey) {
        this.expelledKey = expelledKey;
        this.isHotKey = isHotKey;
        this.currentKey = currentKey;
    }

}
