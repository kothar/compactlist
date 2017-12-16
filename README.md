#CompactList

CompactList implements the `List<Long>` interface, but internally it uses a tree of variable word-width segments to improve
performance and memory usage compared to an `ArrayList`.

Similar primitive container implementations can be found elsewhere, notably:

  * [Trove][1]
  * [Guava Primitive Wrappers][2] ([More info][3])
  
[1]: https://bitbucket.org/trove4j/trove
[2]: https://google.github.io/guava/releases/19.0/api/docs/com/google/common/primitives/Ints.html#asList(int...)
[3]: https://github.com/google/guava/wiki/PrimitivesExplained

## Performance

Performance of `CompactList` tends to be worse than `ArrayList` for small lists, but gains an advantage as list
size increases. This is mainly due to the tree structure which limits the amount of memory that needs to be copied when
elements are inserted or removed, or the allocated backing array is grown during an append.

The implementation currently splits segments at 2^16 elements, which is where performance gains for insertion start to appear.
In the charts below, `CompactList` beats `ArrayList` when inserting ~2^17 or more elements.

Benchmarks were run on a Xeon E3-1220 @3.1GHz running Windows 10 and Oracle Java 1.8.0_152-b16
 
### Append
This benchmark appends sequential values to the end of the list.

![Total time for sequential append](img/appendseq.png)
![Average operation time for sequential append](img/appendseq_op.png)

### Insert
This benchmark inserts sequential values at random locations as the list grows

![Total time for sequential insert](img/insertseq.png)
![Average operation time for sequential insert](img/insertseq_op.png)

### Remove
### Set
### Iteration

## Memory usage

Memory usage depends on how regular the data is, since more regular data can be stored with fewer bits. As a baseline,
non-compacted `CompactLists` on a 64-bit JVM use roughly one third of the memory of an `ArrayList<Long>`
(8 bytes per value vs 4 byte pointer + 24 byte Long object per value). Memory usage after compaction
is close to the underlying storage size for the smallest word width capable of storing the range of values in each segment.

`CompactList` handles regular data such as repeated or ascending values extremely well.

Storage strategies are implemented for word widths of 64, 32, 16, 8, 4 and 0 (constant value).

![Memory usage during append](img/appendseq_mem.png)
![Memory usage during insertion](img/insertseq_mem.png)

## Compaction strategy

The default instantiation of `CompactList` uses a queue of 'dirty' segments which will gradually be compacted as operations
are executed which modify the list structure. This can be disabled by constructing the list with a `NoopNodeManager`.

Lists can be compacted manually by calling `CompactList.compact()`.

When compacting, several strategies are attempted to reduce the storage size needed:

**Linear Prediction** will only record the distance of each point in a segment from a trend line of the form *ax + b*.

**Offset** will attempt to add an offset to each value, shifting the zero-point to the mean value in that segment. It has
an advantage over the linear prediction strategy in that it is *position independent* and inserting or removing values
from a segment will not change the validity of other entries.
