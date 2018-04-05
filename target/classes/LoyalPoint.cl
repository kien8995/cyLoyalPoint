__kernel void Init(__global const int* a, __global const int* b, __global int* c, int n)
{
    int id = get_global_id(0);
    if (id >= n) {
        return;
    }
    c[id] = a[id] + b[id];
}
