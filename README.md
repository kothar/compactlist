#CompactList

CompactList implements the `List<Long>` interface, but internally it uses a tree of `long[]` segments to improve
performance and memory usage compared to an `ArrayList`.

Similar primitive container implementations can be found elsewhere, notably:

  * [Trove][1]
  
[1]: https://bitbucket.org/trove4j/trove

## Performance

### Insert
### Remove
### Set
### Iteration

## Memory usage

Memory usage depends on how regular the data is, since more regular data can be stored with fewer bytes. As a baseline,
non-compacted lists use roughly half the memory of an `ArrayList<Long>`. Memory usage after compaction is close to the
underlying storage size for the smallest word width capable of storing the range of values in each segment.

## Compaction strategy

Nodes are currently compacted manually by calling `CompactList.compact()`