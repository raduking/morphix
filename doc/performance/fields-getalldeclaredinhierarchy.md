### Fields.getAllDeclaredInHierarchy

JMH benchmark comparing different list implementations for collecting
declared fields across a class hierarchy.

| Implementation | Mode | Cnt | Score (ns/op) | Error (± ns/op) | Relative to FastArrayList |
|:---|:---:|---:|---:|---:|---:|
| `ArrayList.addAll` | avgt | 5 | 175.941 | 4.802 | +17.27% |
| `ArrayList.ensureCapacity` | avgt | 5 | 152.947 | 0.857 | +1.94% |
| `ArrayList` loop | avgt | 5 | 164.359 | 0.196 | +9.55% |
| `FastArrayList` | avgt | 5 | **150.034** | 1.574 | **Baseline** |
| `LinkedList` | avgt | 5 | 149.891 | 2.592 | -0.10% |

Lower scores are better.

#### Conclusions

- `FastArrayList` reduces execution time from approximately 170 ns/op
  to 150 ns/op, an improvement of approximately 12%.
- `FastArrayList` matches `LinkedList` construction performance within
  the reported measurement errors.
- Direct bulk insertion of arrays eliminates intermediate collection
  wrappers and temporary source arrays.
- `FastArrayList` retains the advantages of contiguous reference storage,
  O(1) indexed access, and better cache locality compared to `LinkedList`.
- The implementation requires no explicit capacity management or
  element-by-element insertion.

#### Implementation characteristics

| Characteristic | LinkedList | FastArrayList |
|:---|:---|:---|
| Hierarchy construction | ~150 ns | ~150 ns |
| Indexed access | O(n) | O(1) |
| Iteration locality | Poorer | Better |
| Storage | Individual nodes | Contiguous reference array |
| Bulk array insertion | Element by element | `System.arraycopy` |
