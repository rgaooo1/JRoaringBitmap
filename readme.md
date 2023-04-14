```
// 在bitmapContainer.go中iaddReturnMinimized方法中，
如果bitmapContainer满了，就返回一个runContainer16Range，这个方法的实现如下：
func (bc *bitmapContainer) iaddReturnMinimized(i uint16) container {
bc.iadd(i)
if bc.isFull() {
return newRunContainer16Range(0, MaxUint16)
}
return bc
}
所以还是需要能够支持 serialCookie的，否则会出现错误,
现在第一个版本还不支持, 先看Roaring64Bitmap的实现,然后再来一起改
```

* Java的官方版本中, 也不完全兼容Go的版本,
  java在返回集合时用的是数组, 而数组的长度最多是 INT_MAX = 2^31 - 1,
  而go中bitmap的容量可以达到 2^32
* 官方版本在返回value是用的是int, 这样就会导致go中可以存下 value: 1<<32-1, 但是在java里读出来是-1,
  不过应该不影响比较操作(包含/Not/And/Or)
