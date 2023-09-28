# ScotlandYard
This is an implementation of the board game - Scotland Yard, with a AI Mr.X
<br>
<br>
<br>

# Xbot (AI of mr X)
## Documents:
in branch: "ai"

## Ideas：
0. choose a step that detectives can't reach in the next round (MUST)
1. choose a step that is far away from all detectives (by dividing map and using Dijkstra's to calculate distance)
2. make sure mrX is ready for reveal round (go to a stop with more transportation posibilities)
3. use black card and double move after reveal

## Algorithm
A* + Alpha-Beta pruning

## Links
0. https://www.cnblogs.com/KillerAery/p/10283768.html#a-%E5%AF%BB%E8%B7%AF%E7%AE%97%E6%B3%95
