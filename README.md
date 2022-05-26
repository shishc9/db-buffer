# db-buffer

[简易缓冲池实现，实现了多种页面替换算法、文件存储策略、缓冲池管理]

### icu.shishc.disk
    文件存储模块。
    实现了数据页文件(.db)与数据文件映射(.db.map)
### icu.shishc.replacer
    页面替换算法模块
    LRUReplacer: LRU页面替换算法.
    LFUReplacer: LFU页面替换算法.
    SingleListLRUReplacer: 基于单链表的LRU. - 测试中未使用
    ConcurrentLRUReplacer: 高并发环境下LRU.
        - 基于哈希函数将数据平均分在每个缓存段.
    HotColdSeparationLRUReplacer: 冷热分离链 - 未实现.
### icu.shishc.bufferpool
    缓冲池模块
### icu.shishc.test
    JUnit - 功能测试
    JMH - 测试算法性能 - 吞吐量、平均执行时间、命中率.
