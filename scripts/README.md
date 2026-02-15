# Scripts Directory

This directory contains utility scripts for monitoring and managing the Jib Pilot application.

## JVM Metrics Monitor

### `jvm-metrics.sh`

A comprehensive script that collects and displays JVM metrics from Spring Boot Actuator endpoints.

#### Features

- **Memory Metrics**: Heap, non-heap, metaspace, compressed class space, code cache
- **Thread Metrics**: Live threads, peak threads, daemon threads, thread states
- **GC Metrics**: Garbage collection pause times, memory operations
- **Class Loading**: Loaded and unloaded class counts
- **Database Pool**: HikariCP connection pool metrics
- **System Metrics**: CPU usage, uptime
- **Health Indicators**: Automatic warnings and recommendations

#### Requirements

- `jq` - JSON processor
  - macOS: `brew install jq`
  - Linux: `sudo apt-get install jq` or `sudo yum install jq`
- `curl` - Usually pre-installed
- Spring Boot Actuator enabled

#### Usage

```bash
# Default (http://localhost:8085)
./scripts/jvm-metrics.sh

# Custom URL
./scripts/jvm-metrics.sh http://localhost:8080
./scripts/jvm-metrics.sh https://api.example.com
```

#### Example Output

The script provides color-coded, formatted output showing:

- Memory usage with percentages and warnings
- Thread counts and states
- GC performance metrics
- Database connection pool status
- System resource usage
- Health status with recommendations

#### Color Coding

- 🟢 **Green**: Healthy/normal values
- 🟡 **Yellow**: Warning levels (80-90% usage)
- 🔴 **Red**: Critical levels (>90% usage or errors)
- 🔵 **Cyan**: Informational values

#### Integration

You can integrate this script into:

- **CI/CD pipelines**: Run before/after deployments
- **Monitoring dashboards**: Parse output for metrics
- **Health checks**: Use exit codes (0 = healthy, 1 = error)
- **Scheduled tasks**: Cron jobs for regular monitoring

#### Exit Codes

- `0`: Success - metrics collected successfully
- `1`: Error - connection failed or dependencies missing

#### Tips

1. **Baseline Collection**: Run before optimizations to establish baseline
2. **Regular Monitoring**: Schedule periodic runs to track trends
3. **Comparison**: Save output to files for before/after comparison
4. **Automation**: Use in scripts to trigger alerts on warnings

#### Example: Save Metrics to File

```bash
# Save with timestamp
./scripts/jvm-metrics.sh > metrics-$(date +%Y%m%d-%H%M%S).txt

# Compare before/after
./scripts/jvm-metrics.sh > metrics-before.txt
# ... make optimizations ...
./scripts/jvm-metrics.sh > metrics-after.txt
diff metrics-before.txt metrics-after.txt
```

#### Troubleshooting

**Error: Cannot connect to Actuator**
- Verify application is running
- Check Actuator is enabled in `application.yml`
- Verify the base URL is correct
- Check firewall/network settings

**Error: jq is not installed**
- Install jq using package manager
- Verify installation: `jq --version`

**Metrics show "null"**
- Some metrics may not be available if:
  - GC logging is not enabled
  - Certain features are disabled
  - Application just started (some metrics need time to accumulate)

## Understanding the Metrics

This section explains what each metric means and how to interpret the values.

### Memory Metrics

#### Heap Memory
- **Used**: Current heap memory in use by your application objects
- **Max**: Maximum heap size configured (or available)
- **Committed**: Memory guaranteed to be available to the JVM
- **Usage**: Percentage of max heap currently used

**Interpretation:**
- ✅ **< 70%**: Healthy - plenty of headroom
- ⚠️ **70-80%**: Warning - monitor closely, may need optimization
- 🔴 **> 80%**: Critical - risk of OutOfMemoryError, immediate action needed

**Example from output:**
```
Used: 283.98 MB
Max: 2.96 GB
Usage: 9.4%
```
This shows healthy memory usage - only 9.4% of available heap is used.

#### Heap Memory by Generation (G1GC)
Shows memory breakdown for G1 Garbage Collector regions:
- **Eden Space**: Where new objects are allocated
- **Survivor Space**: Objects that survived minor GC
- **Old Generation**: Long-lived objects

**Note**: If these don't appear, the application may not be using G1GC or metrics aren't available.

#### Non-Heap Memory
Memory used outside the heap:
- **Used**: Current non-heap usage
- **Max**: Maximum non-heap size
- **Committed**: Guaranteed non-heap memory
- **Usage**: Percentage of max non-heap used

**Interpretation:**
- ✅ **< 80%**: Normal
- ⚠️ **80-90%**: Warning
- 🔴 **> 90%**: Critical - may indicate class loading issues

#### Metaspace
Stores class metadata, method information, and constant pools.

**Interpretation:**
- ✅ **< 80%**: Healthy
- ⚠️ **80-90%**: Warning - consider increasing MaxMetaspaceSize
- 🔴 **> 90%**: Critical - risk of MetaspaceOutOfMemoryError

**Example from output:**
```
Used: 285.62 MB
Max: 2.96 GB
Usage: 9.4%
```
This is healthy - only 9.4% of metaspace is used. High metaspace usage often indicates too many loaded classes (unused dependencies).

#### Compressed Class Space
Memory for compressed class pointers (only on 64-bit JVMs with compressed OOPs).

#### Code Cache
Memory used by the JIT compiler to store compiled native code.

**Interpretation:**
- High usage (> 90%) may indicate aggressive JIT compilation
- Usually not a concern unless consistently high

### Thread Metrics

#### Live Threads
Current number of active threads in the JVM.

**Interpretation:**
- ✅ **< 100**: Normal for most applications
- ⚠️ **100-200**: Monitor - may indicate thread pool misconfiguration
- 🔴 **> 200**: High - likely thread leak or inefficient thread usage

**Example from output:**
```
Live Threads: 34.0
Peak Threads: 35.0
```
This is excellent - very low thread count indicates efficient resource usage.

#### Peak Threads
Maximum number of threads that have been active since startup.

#### Daemon Threads
Background threads that don't prevent JVM shutdown.

#### Thread States
Breakdown of threads by their current state:
- **Runnable**: Threads ready to run or currently executing
- **Blocked**: Threads waiting for a monitor lock
- **Waiting**: Threads waiting indefinitely for another thread
- **Timed Waiting**: Threads waiting with a timeout
- **New**: Threads that haven't started yet
- **Terminated**: Threads that have completed execution

**Interpretation:**
- High **Blocked** count: Possible lock contention or deadlock risk
- High **Waiting** count: May indicate resource contention
- Most threads should be **Runnable** or **Timed Waiting** under normal load

**Note**: If all states show the same value (e.g., all 34.0), this indicates the metric query is returning the total thread count rather than per-state counts. This is a known limitation with some Actuator configurations. The sum of all individual state counts should equal Live Threads. In the example output showing all states as 34.0, this suggests the query needs refinement or the metric isn't properly tagged by state.

### Garbage Collection Metrics

#### GC Pause Times
Time spent in garbage collection pauses.

**Interpretation:**
- ✅ **< 100ms**: Excellent
- ⚠️ **100-200ms**: Acceptable
- 🔴 **> 200ms**: Poor - may cause noticeable latency

**Note**: Requires GC logging to be enabled for detailed metrics.

#### GC Memory Operations
- **Memory Allocated**: Total memory allocated since startup
- **Memory Promoted**: Memory moved from young to old generation

**Example from output:**
```
Memory Allocated: 355.00 MB
Memory Promoted: 6.89 MB
```
This shows relatively low memory allocation and promotion, indicating efficient memory usage.

### Class Loading Metrics

#### Loaded Classes
Total number of classes currently loaded in the JVM.

**Interpretation:**
- ✅ **< 20,000**: Normal for most applications
- ⚠️ **20,000-30,000**: High - may indicate unused dependencies
- 🔴 **> 30,000**: Very high - likely unnecessary class loading

**Example from output:**
```
Loaded Classes: 24960.0
```
This is on the higher side. After optimization (removing unused dependencies), this should decrease significantly.

#### Unloaded Classes
Classes that have been garbage collected (usually 0 in long-running applications).

### Database Connection Pool (HikariCP)

#### Active Connections
Currently in-use database connections.

#### Idle Connections
Available connections in the pool.

#### Pending Connections
Requests waiting for a connection (should be 0).

**Interpretation:**
- ✅ **Pending = 0**: Healthy
- 🔴 **Pending > 0**: Pool may be too small - increase `maximum-pool-size`

#### Max Pool Size
Maximum number of connections allowed in the pool.

**Example from output:**
```
(No metrics shown)
```
If this section is empty, HikariCP metrics may not be exposed or the pool hasn't been used yet.

### System Metrics

#### System CPU Usage
Overall CPU usage across all cores.

**Interpretation:**
- ✅ **< 50%**: Normal
- ⚠️ **50-80%**: High load
- 🔴 **> 80%**: Very high - may indicate performance issues

#### CPU Cores
Number of available CPU cores.

#### Process CPU Usage
CPU usage by this specific Java process.

**Example from output:**
```
System CPU Usage: 1.9%
Process CPU Usage: 0.1%
```
This shows very low CPU usage, indicating the application is idle or handling light load.

#### Uptime
How long the application has been running.

### Summary & Health Indicators

The script automatically analyzes metrics and provides:
- **Status**: Overall health (HEALTHY, WARNING, CRITICAL)
- **Warnings**: Specific issues detected
- **Recommendations**: Suggested actions to improve

**Example from output:**
```
✓ Status: HEALTHY
```
This indicates all metrics are within acceptable ranges.

## Interpreting Your Current Metrics

Based on the example output shown:

### ✅ **Healthy Indicators:**
- **Heap Usage: 9.4%** - Excellent, plenty of headroom
- **Thread Count: 34** - Very low, efficient resource usage
- **CPU Usage: 0.1%** - Application is idle or handling light load
- **Memory Allocation: 355 MB** - Reasonable for a Spring Boot app

### ⚠️ **Areas for Optimization:**
- **Loaded Classes: 24,960** - High, suggests unused dependencies
  - **Action**: Run dependency analysis to identify and remove unused libraries
- **Metaspace: 285 MB** - Could be reduced with dependency cleanup
  - **Action**: Remove unused transitive dependencies

### 📊 **Expected After Optimization:**
Based on the optimization plan:
- **Loaded Classes**: Should drop to ~15,000-18,000 (30-40% reduction)
- **Heap Usage**: Should remain similar or improve slightly
- **Metaspace**: Should decrease with fewer loaded classes
- **Thread Count**: Should remain similar (already optimized)

## Metric Comparison Guide

When comparing before/after optimization:

1. **Memory Metrics**: Look for reductions in:
   - Heap usage percentage (should stay similar or improve)
   - Metaspace usage (should decrease)
   - Total memory footprint

2. **Class Loading**: 
   - Loaded classes should decrease significantly
   - Indicates successful dependency cleanup

3. **Thread Metrics**:
   - Should remain stable or decrease slightly
   - Large increases may indicate issues

4. **GC Metrics**:
   - Pause times should improve or stay similar
   - Memory promotion should decrease with better object lifecycle management

5. **System Metrics**:
   - CPU usage should remain similar or improve
   - Lower is better, but depends on workload
