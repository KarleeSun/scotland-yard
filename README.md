# Xbot (AI of mr X)

## Ideas：
0. 走到的地方不能让detectives下一步走到（必须）
0.5. 如果0不能满足，但是mrX还没死，那就随机一个位置
1. 尽量走到离detectives远的地方（分区&用图最短路径算法计算距离）
2. 在reveal之前看好往哪跑（尽量去大站）
3. reveal之后用黑卡和double move

## Algorithm
A* + Alpha-Beta pruning

## Links
0. 寻路算法
https://www.cnblogs.com/KillerAery/p/10283768.html#a-%E5%AF%BB%E8%B7%AF%E7%AE%97%E6%B3%95
