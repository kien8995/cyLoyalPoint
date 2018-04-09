#define EPS 1e-7f

__kernel void Init(__global int* a, __global int* b, __global int* c)
{
    int id = get_global_id(0);
    c[id] = a[id] + b[id];
}

//int getNumberOfAdjacent(int* adjList, int node) {
//    if(adjList[0] == -1) {
//        return 0;
//    }
//    return adjList[node + 1] - adjList[node];
//}
//
//int getCurrentNodeIndex(int* adjList, int node, int index) {
//    return adjList[adjList[node] + index];
//}
//
//float absFloat(float num)
//{
//    if(num < 0.0) {
//        return -num;
//    }
//    return num;
//}
//
//void swapArray(float* sourceArr, float* destArr, int size)
//{
//    float* temp = sourceArr;
//    sourceArr = destArr;
//    destArr = temp;
//}
//
//float ComputeCompetitive(__global const int* unDirectedAdjacentList, __global const int* inDirectedAdjacentList, int leader, int againstLeader, int nodeCount, float E, int targetNode)
//{
//    int maxIterations = 1000;
//    float loyalPoint[1000];
//    float tempLoyalPoint[1000];
//
//    for (int node = 0; node < nodeCount + 1; node++) {
//        loyalPoint[node] = 0.0f; // random value in {-1, 0, 1}
//        tempLoyalPoint[node] = 0.0f;
//    }
//
//    loyalPoint[leader] = 1.0f;
//    tempLoyalPoint[leader] = 1.0f;
//
//    loyalPoint[againstLeader] = -1.0f;
//    tempLoyalPoint[againstLeader] = -1.0f;
//
//    float error = 0.0f;
//    int iter = 0;
//
//    do {
//        error = 0.0f;
//        for (int currentNode = 0; currentNode < nodeCount + 1; currentNode++) {
//            float currentLoyalPoint = loyalPoint[currentNode];
//
//            if (currentNode == leader || currentNode == againstLeader) {
//                continue;
//            }
//
//            int nInUnNodeList = getNumberOfAdjacent(&unDirectedAdjacentList, currentNode);
//            int inUnNodeList[nInUnNodeList];
//
//            int inUnNodeListIndex = 0;
////            for (int index = 0; index < getNumberOfAdjacent(&inDirectedAdjacentList, currentNode); index++) {
////                inUnNodeList[inUnNodeListIndex++] = getCurrentNodeIndex(&inDirectedAdjacentList, currentNode, index);
////            }
//            for (int index = 0; index < getNumberOfAdjacent(&unDirectedAdjacentList, currentNode); index++) {
//                inUnNodeList[inUnNodeListIndex++] = getCurrentNodeIndex(&unDirectedAdjacentList, currentNode, index);
//            }
//
//            float sum = 0.0f;
//            for (int index = 0; index < nInUnNodeList; index++) {
//                int neighbourNode = inUnNodeList[index];
//                float weight = 1.0f;
//
//                sum += weight * (loyalPoint[neighbourNode] - currentLoyalPoint);
//            }
//
//            if (targetNode == currentNode) {
//                int neighbourNode = againstLeader;
//                float weight = 1.0f;
//
//                sum += weight * (loyalPoint[neighbourNode] - currentLoyalPoint);
//            }
//
//            float newLoyalPoint = currentLoyalPoint + E * sum;
//            tempLoyalPoint[currentNode] = newLoyalPoint;
//            error += absFloat(newLoyalPoint - currentLoyalPoint);
//        }
//
//        for (int i = 0; i < nodeCount + 1; i++) {
//            float temp = loyalPoint[i];
//            loyalPoint[i] = tempLoyalPoint[i];
//            tempLoyalPoint[i] = temp;
//        }
//
//        iter++;
//    } while (error > EPS && iter < maxIterations);
//
//    return loyalPoint[targetNode];
//}
//
//__kernel void ComputeLoyalNodesOfLeader(__global const int* unDirectedAdjacentList, __global const int* inDirectedAdjacentList, __global const int* normalNodes, __global float* result, int leader, int againstLeader, int nodeCount, float E, int n) {
//
//    int currentThreadId = get_global_id(0);
//
//    if (currentThreadId >= n) {
//        return;
//    }
//
//    int targetNode = normalNodes[currentThreadId];
//
//
//
//    ///
//    result[targetNode]  = ComputeCompetitive(unDirectedAdjacentList, inDirectedAdjacentList, leader, againstLeader, nodeCount, E, targetNode);
//    ///
//}

//////////////////////////////////////////
__kernel void ComputeSums(__global const int* inDirectedAdjacentList,
                          __global const int* unDirectedAdjacentList,
                          __global float* loyalPoint,
                          __global float* sums,
                          int leader,
                          int againstLeader,
                          int targetNode,
                          int n)
{
    int currentNode = get_global_id(0);
    
    if(currentNode >= n || currentNode == leader || currentNode == againstLeader)
    {
        return;
    }
    
    float sum = 0.0f;

    int nInDirectedList = inDirectedAdjacentList[0] == -1 ? 0 : inDirectedAdjacentList[currentNode + 1] - inDirectedAdjacentList[currentNode];
    for (int index = 0; index < nInDirectedList; index++) {
        int neighbourNode = inDirectedAdjacentList[inDirectedAdjacentList[currentNode] + index];
        float weight = 1.0f;

        sum += weight * (loyalPoint[neighbourNode] - loyalPoint[currentNode]);
    }

    int nUnDirectedList = unDirectedAdjacentList[0] == -1 ? 0 : unDirectedAdjacentList[currentNode + 1] - unDirectedAdjacentList[currentNode];
    for (int index = 0; index < nUnDirectedList; index++) {
        int neighbourNode = unDirectedAdjacentList[unDirectedAdjacentList[currentNode] + index];
        float weight = 1.0f;

        sum += weight * (loyalPoint[neighbourNode] - loyalPoint[currentNode]);
    }
    
    if (currentNode == targetNode) {
        int neighbourNode = againstLeader;
        float weight = 1.0f;
        
        sum += weight * (loyalPoint[neighbourNode] - loyalPoint[currentNode]);
    }
    
    sums[currentNode] = sum;
}

__kernel void ComputeLoyalPoint(__global float* loyalPoint,
                                __global float* sums,
                                __global float* errors,
                                int leader,
                                int againstLeader,
                                float E,
                                int n)
{
    int currentNode = get_global_id(0);
    
    if(currentNode >= n || currentNode == leader || currentNode == againstLeader)
    {
        return;
    }
    
    float oldLoyalPoint = loyalPoint[currentNode];
    float newLoyalPoint = oldLoyalPoint + E * sums[currentNode];
    
    errors[currentNode] = fabs(newLoyalPoint - oldLoyalPoint) > errors[currentNode] ? fabs(newLoyalPoint - oldLoyalPoint) : errors[currentNode];
    loyalPoint[currentNode] = newLoyalPoint;
}
