# Xbot (AI of mr X)

## Ideas：
0. 走到的地方不能让detectives下一步走到（必须）
0.5. 如果0不能满足，但是mrX还没死，那就随机一个位置
1. 尽量走到离detectives远的地方（分区&用图最短路径算法计算距离）
2. 在reveal之前看好往哪跑（尽量去大站）
3. reveal之后用黑卡和double move

## Algorithm
0. A* + Alpha-Beta pruning
1. Dijkstra heap algorithm + Alpha-Beta pruning

## Links
0. 寻路算法
https://www.cnblogs.com/KillerAery/p/10283768.html#a-%E5%AF%BB%E8%B7%AF%E7%AE%97%E6%B3%95
1. minheap+dijkstra
https://www.geeksforgeeks.org/dijkstras-algorithm-for-adjacency-list-representation-greedy-algo-8/

## Marks
0. available moves和detectives的距离
1. 这个点有几种交通工具 都是哪些
2. detectives还剩什么票
3. 和这个点相连的点有几个
4. reveal

## Conclusions
0. 什么时候用黑卡
1. 什么时候用double move
2. 选哪个点最好
