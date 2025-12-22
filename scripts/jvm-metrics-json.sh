#!/bin/bash

###############################################################################
# JVM Metrics JSON Export Script
# 
# This script collects JVM metrics and exports them as JSON for programmatic
# use, integration with monitoring systems, or data analysis.
#
# Usage:
#   ./scripts/jvm-metrics-json.sh [BASE_URL] [OUTPUT_FILE]
#
# Example:
#   ./scripts/jvm-metrics-json.sh http://localhost:8085 metrics.json
#
# Requirements:
#   - jq (JSON processor)
#   - curl
#   - Spring Boot Actuator enabled
###############################################################################

set -euo pipefail

BASE_URL="${1:-http://localhost:8085}"
OUTPUT_FILE="${2:-jvm-metrics-$(date +%Y%m%d-%H%M%S).json}"
ACTUATOR_BASE="${BASE_URL}/actuator"

# Check dependencies
if ! command -v jq &> /dev/null; then
    echo "Error: jq is not installed" >&2
    exit 1
fi

if ! command -v curl &> /dev/null; then
    echo "Error: curl is not installed" >&2
    exit 1
fi

# Function to get metric value
get_metric() {
    local metric_name=$1
    local tag_filter=${2:-}
    local url="${ACTUATOR_BASE}/metrics/${metric_name}"
    
    if [ -n "$tag_filter" ]; then
        url="${url}?${tag_filter}"
    fi
    
    curl -s "$url" 2>/dev/null || echo "{}"
}

# Function to get metric with tag
get_metric_with_tag() {
    local metric_name=$1
    local tag_key=$2
    local tag_value=$3
    get_metric "$metric_name" "${tag_key}=${tag_value}"
}

# Check actuator accessibility
if ! curl -s -f "${ACTUATOR_BASE}/health" > /dev/null 2>&1; then
    echo "Error: Cannot connect to Actuator at ${ACTUATOR_BASE}" >&2
    exit 1
fi

# Collect all metrics
echo "Collecting metrics from ${BASE_URL}..." >&2

# Build JSON object
jq -n \
  --arg timestamp "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
  --arg base_url "$BASE_URL" \
  --argjson heap_used "$(get_metric_with_tag 'jvm.memory.used' 'area' 'heap' | jq '.measurements[0].value // 0')" \
  --argjson heap_max "$(get_metric_with_tag 'jvm.memory.max' 'area' 'heap' | jq '.measurements[0].value // 0')" \
  --argjson heap_committed "$(get_metric_with_tag 'jvm.memory.committed' 'area' 'heap' | jq '.measurements[0].value // 0')" \
  --argjson nonheap_used "$(get_metric_with_tag 'jvm.memory.used' 'area' 'nonheap' | jq '.measurements[0].value // 0')" \
  --argjson nonheap_max "$(get_metric_with_tag 'jvm.memory.max' 'area' 'nonheap' | jq '.measurements[0].value // 0')" \
  --argjson nonheap_committed "$(get_metric_with_tag 'jvm.memory.committed' 'area' 'nonheap' | jq '.measurements[0].value // 0')" \
  --argjson metaspace_used "$(get_metric_with_tag 'jvm.memory.used' 'id' 'Metaspace' | jq '.measurements[0].value // 0')" \
  --argjson metaspace_max "$(get_metric_with_tag 'jvm.memory.max' 'id' 'Metaspace' | jq '.measurements[0].value // 0')" \
  --argjson threads_live "$(get_metric 'jvm.threads.live' | jq '.measurements[0].value // 0')" \
  --argjson threads_peak "$(get_metric 'jvm.threads.peak' | jq '.measurements[0].value // 0')" \
  --argjson threads_daemon "$(get_metric 'jvm.threads.daemon' | jq '.measurements[0].value // 0')" \
  --argjson classes_loaded "$(get_metric 'jvm.classes.loaded' | jq '.measurements[0].value // 0')" \
  --argjson classes_unloaded "$(get_metric 'jvm.classes.unloaded' | jq '.measurements[0].value // 0')" \
  --argjson hikari_active "$(get_metric 'hikari.connections.active' | jq '.measurements[0].value // 0')" \
  --argjson hikari_idle "$(get_metric 'hikari.connections.idle' | jq '.measurements[0].value // 0')" \
  --argjson hikari_max "$(get_metric 'hikari.connections.max' | jq '.measurements[0].value // 0')" \
  --argjson cpu_usage "$(get_metric 'system.cpu.usage' | jq '.measurements[0].value // 0')" \
  --argjson cpu_count "$(get_metric 'system.cpu.count' | jq '.measurements[0].value // 0')" \
  --argjson process_cpu_usage "$(get_metric 'process.cpu.usage' | jq '.measurements[0].value // 0')" \
  --argjson uptime "$(get_metric 'process.uptime' | jq '.measurements[0].value // 0')" \
  '{
    timestamp: $timestamp,
    base_url: $base_url,
    memory: {
      heap: {
        used: $heap_used,
        max: $heap_max,
        committed: $heap_committed,
        usage_percent: (if $heap_max > 0 then ($heap_used / $heap_max * 100) else 0 end)
      },
      nonheap: {
        used: $nonheap_used,
        max: $nonheap_max,
        committed: $nonheap_committed,
        usage_percent: (if $nonheap_max > 0 then ($nonheap_used / $nonheap_max * 100) else 0 end)
      },
      metaspace: {
        used: $metaspace_used,
        max: $metaspace_max,
        usage_percent: (if $metaspace_max > 0 then ($metaspace_used / $metaspace_max * 100) else 0 end)
      }
    },
    threads: {
      live: $threads_live,
      peak: $threads_peak,
      daemon: $threads_daemon
    },
    class_loading: {
      loaded: $classes_loaded,
      unloaded: $classes_unloaded
    },
    database: {
      connections: {
        active: $hikari_active,
        idle: $hikari_idle,
        max: $hikari_max,
        usage_percent: (if $hikari_max > 0 then ($hikari_active / $hikari_max * 100) else 0 end)
      }
    },
    system: {
      cpu: {
        usage: $cpu_usage,
        count: $cpu_count,
        process_usage: $process_cpu_usage
      },
      uptime_seconds: $uptime
    },
    health: {
      heap_healthy: ($heap_max > 0 and ($heap_used / $heap_max) < 0.8),
      metaspace_healthy: ($metaspace_max > 0 and ($metaspace_used / $metaspace_max) < 0.9),
      threads_healthy: ($threads_live < 200),
      pool_healthy: ($hikari_max > 0 and ($hikari_active / $hikari_max) < 0.8)
    }
  }' > "$OUTPUT_FILE"

echo "Metrics exported to: ${OUTPUT_FILE}" >&2
echo "$OUTPUT_FILE"
