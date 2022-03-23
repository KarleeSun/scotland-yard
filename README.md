# ScotlandYard
## Questions：
0. get winner还没有写好

## Need to be done:
0. get winner
1. 测试游戏是否能玩
2. report

## Reminders:
0. 都写完之后要对一下格式和缩进
1. 删掉没用的sout
2. 把用piece得到player的循环都用getPlayer函数代替


# Xbot (AI of mr X)
## Documents:
就在这开一个新的branch 上传上去好了

## Ideas：
0. 走到的地方不能让detectives下一步走到（必须）
0.5. 如果0不能满足，但是mrX还没死，那就随机一个位置
1. 尽量走到离detectives远的地方（分区&用图最短路径算法计算距离）
2. 在reveal之前看好往哪跑（尽量去大站）
3. reveal之后用黑卡和double move
