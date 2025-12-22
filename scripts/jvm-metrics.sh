#!/bin/bash

###############################################################################
# JVM Metrics Monitor Script
# 
# This script collects and displays comprehensive JVM metrics from Spring Boot
# Actuator endpoints. It provides a clean, readable format for monitoring
# memory usage, threads, GC, and other JVM-related information.
#
# Usage:
#   ./scripts/jvm-metrics.sh [BASE_URL]
#
# Example:
#   ./scripts/jvm-metrics.sh http://localhost:8085
#
# Requirements:
#   - jq (JSON processor) - install with: brew install jq (macOS) or apt-get install jq (Linux)
#   - curl
#   - Spring Boot Actuator enabled
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Default base URL
BASE_URL="${1:-http://localhost:8085}"
ACTUATOR_BASE="${BASE_URL}/actuator"

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo -e "${RED}Error: jq is not installed.${NC}"
    echo "Please install jq:"
    echo "  macOS: brew install jq"
    echo "  Linux: sudo apt-get install jq"
    exit 1
fi

# Check if curl is available
if ! command -v curl &> /dev/null; then
    echo -e "${RED}Error: curl is not installed.${NC}"
    exit 1
fi

# Function to format bytes to human-readable format
format_bytes() {
    local bytes=$1
    if [ -z "$bytes" ] || [ "$bytes" = "null" ]; then
        echo "N/A"
        return
    fi
    
    # Convert to integer (handle floats and scientific notation)
    local bytes_int=$(echo "$bytes" | awk '{printf "%.0f", $1}')
    
    if [ "$bytes_int" -ge 1073741824 ] 2>/dev/null; then
        echo "$(echo "$bytes" | awk '{printf "%.2f GB", $1/1073741824}')"
    elif [ "$bytes_int" -ge 1048576 ] 2>/dev/null; then
        echo "$(echo "$bytes" | awk '{printf "%.2f MB", $1/1048576}')"
    elif [ "$bytes_int" -ge 1024 ] 2>/dev/null; then
        echo "$(echo "$bytes" | awk '{printf "%.2f KB", $1/1024}')"
    else
        echo "${bytes_int} B"
    fi
}

# Function to calculate percentage
calculate_percentage() {
    local used=$1
    local max=$2
    if [ -z "$used" ] || [ "$used" = "null" ] || [ -z "$max" ] || [ "$max" = "null" ]; then
        echo "N/A"
        return
    fi
    # Use awk to handle floats and check for zero
    echo "$used $max" | awk '{if ($2 == 0) print "N/A"; else printf "%.1f", ($1/$2)*100}'
}

# Function to get metric value
get_metric() {
    local metric_name=$1
    local tag_filter=${2:-}
    local url="${ACTUATOR_BASE}/metrics/${metric_name}"
    
    if [ -n "$tag_filter" ]; then
        url="${url}?${tag_filter}"
    fi
    
    local response=$(curl -s "$url" 2>/dev/null)
    
    if [ $? -ne 0 ] || [ -z "$response" ]; then
        echo "null"
        return
    fi
    
    # Check if response is valid JSON and has measurements
    if echo "$response" | jq -e '.measurements[0].value' > /dev/null 2>&1; then
        echo "$response" | jq -r '.measurements[0].value // empty'
    else
        echo "null"
    fi
}

# Function to get metric with tag
get_metric_with_tag() {
    local metric_name=$1
    local tag_key=$2
    local tag_value=$3
    get_metric "$metric_name" "${tag_key}=${tag_value}"
}

# Function to check if actuator is accessible
check_actuator() {
    if ! curl -s -f "${ACTUATOR_BASE}/health" > /dev/null 2>&1; then
        echo -e "${RED}Error: Cannot connect to Actuator at ${ACTUATOR_BASE}${NC}"
        echo "Please ensure:"
        echo "  1. The application is running"
        echo "  2. Actuator is enabled"
        echo "  3. The base URL is correct (current: ${BASE_URL})"
        exit 1
    fi
}

# Print header
print_header() {
    local title=$1
    echo ""
    echo -e "${BOLD}${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BOLD}${CYAN}  ${title}${NC}"
    echo -e "${BOLD}${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

# Print section
print_section() {
    local title=$1
    echo ""
    echo -e "${BOLD}${BLUE}▶ ${title}${NC}"
    echo "────────────────────────────────────────────────────────────────────────────"
}

# Print metric row
print_metric() {
    local label=$1
    local value=$2
    local unit=${3:-}
    local color=${4:-NC}
    
    printf "  %-35s " "$label:"
    echo -e "${!color}${value}${unit}${NC}"
}

# Main execution
main() {
    echo -e "${BOLD}${GREEN}"
    echo "╔══════════════════════════════════════════════════════════════════════════╗"
    echo "║                    JVM Metrics Monitor                                    ║"
    echo "║                    Spring Boot Actuator                                  ║"
    echo "╚══════════════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
    
    echo -e "${YELLOW}Connecting to: ${BASE_URL}${NC}"
    echo -e "${YELLOW}Timestamp: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    
    # Check actuator accessibility
    check_actuator
    
    # Get application info
    local app_info=$(curl -s "${ACTUATOR_BASE}/info" 2>/dev/null || echo "{}")
    local app_name=$(echo "$app_info" | jq -r '.app.name // "jib-pilot"')
    
    echo -e "${GREEN}✓ Connected to ${app_name}${NC}"
    
    ###############################################################################
    # MEMORY METRICS
    ###############################################################################
    print_header "MEMORY METRICS"
    
    # Heap Memory
    print_section "Heap Memory"
    local heap_used=$(get_metric_with_tag "jvm.memory.used" "area" "heap")
    local heap_max=$(get_metric_with_tag "jvm.memory.max" "area" "heap")
    local heap_committed=$(get_metric_with_tag "jvm.memory.committed" "area" "heap")
    local heap_usage_pct=$(calculate_percentage "$heap_used" "$heap_max")
    
    print_metric "Used" "$(format_bytes "$heap_used")" "" "YELLOW"
    print_metric "Max" "$(format_bytes "$heap_max")" "" "CYAN"
    print_metric "Committed" "$(format_bytes "$heap_committed")" "" "CYAN"
    local heap_int=$(echo "$heap_usage_pct" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
    local heap_color="GREEN"
    if [ "$heap_int" -gt 80 ] 2>/dev/null; then
        heap_color="RED"
    elif [ "$heap_int" -gt 70 ] 2>/dev/null; then
        heap_color="YELLOW"
    fi
    print_metric "Usage" "${heap_usage_pct}%" "" "$heap_color"
    
    # Heap Memory by Generation (G1GC)
    print_section "Heap Memory by Generation (G1GC)"
    
    local eden_used=$(get_metric_with_tag "jvm.memory.used" "id" "G1 Eden Space")
    local eden_max=$(get_metric_with_tag "jvm.memory.max" "id" "G1 Eden Space")
    local survivor_used=$(get_metric_with_tag "jvm.memory.used" "id" "G1 Survivor Space")
    local survivor_max=$(get_metric_with_tag "jvm.memory.max" "id" "G1 Survivor Space")
    local old_gen_used=$(get_metric_with_tag "jvm.memory.used" "id" "G1 Old Gen")
    local old_gen_max=$(get_metric_with_tag "jvm.memory.max" "id" "G1 Old Gen")
    
    if [ "$eden_used" != "null" ] && [ "$eden_used" != "0" ]; then
        local eden_pct=$(calculate_percentage "$eden_used" "$eden_max")
        print_metric "Eden Space" "$(format_bytes "$eden_used") / $(format_bytes "$eden_max")" " (${eden_pct}%)" "CYAN"
    fi
    
    if [ "$survivor_used" != "null" ] && [ "$survivor_used" != "0" ]; then
        local survivor_pct=$(calculate_percentage "$survivor_used" "$survivor_max")
        print_metric "Survivor Space" "$(format_bytes "$survivor_used") / $(format_bytes "$survivor_max")" " (${survivor_pct}%)" "CYAN"
    fi
    
    if [ "$old_gen_used" != "null" ] && [ "$old_gen_used" != "0" ]; then
        local old_gen_pct=$(calculate_percentage "$old_gen_used" "$old_gen_max")
        local old_gen_int=$(echo "$old_gen_pct" | awk '{printf "%.0f", $1}')
        local old_gen_color="CYAN"
        if [ "$old_gen_int" -gt 80 ] 2>/dev/null; then
            old_gen_color="RED"
        fi
        print_metric "Old Generation" "$(format_bytes "$old_gen_used") / $(format_bytes "$old_gen_max")" " (${old_gen_pct}%)" "$old_gen_color"
    fi
    
    # Non-Heap Memory
    print_section "Non-Heap Memory"
    local nonheap_used=$(get_metric_with_tag "jvm.memory.used" "area" "nonheap")
    local nonheap_max=$(get_metric_with_tag "jvm.memory.max" "area" "nonheap")
    local nonheap_committed=$(get_metric_with_tag "jvm.memory.committed" "area" "nonheap")
    local nonheap_usage_pct=$(calculate_percentage "$nonheap_used" "$nonheap_max")
    
    print_metric "Used" "$(format_bytes "$nonheap_used")" "" "YELLOW"
    print_metric "Max" "$(format_bytes "$nonheap_max")" "" "CYAN"
    print_metric "Committed" "$(format_bytes "$nonheap_committed")" "" "CYAN"
    local nonheap_int=$(echo "$nonheap_usage_pct" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
    local nonheap_color="GREEN"
    if [ "$nonheap_int" -gt 90 ] 2>/dev/null; then
        nonheap_color="RED"
    elif [ "$nonheap_int" -gt 80 ] 2>/dev/null; then
        nonheap_color="YELLOW"
    fi
    print_metric "Usage" "${nonheap_usage_pct}%" "" "$nonheap_color"
    
    # Metaspace
    print_section "Metaspace"
    local metaspace_used=$(get_metric_with_tag "jvm.memory.used" "id" "Metaspace")
    local metaspace_max=$(get_metric_with_tag "jvm.memory.max" "id" "Metaspace")
    local metaspace_committed=$(get_metric_with_tag "jvm.memory.committed" "id" "Metaspace")
    local metaspace_pct=$(calculate_percentage "$metaspace_used" "$metaspace_max")
    
    print_metric "Used" "$(format_bytes "$metaspace_used")" "" "YELLOW"
    print_metric "Max" "$(format_bytes "$metaspace_max")" "" "CYAN"
    print_metric "Committed" "$(format_bytes "$metaspace_committed")" "" "CYAN"
    local metaspace_int=$(echo "$metaspace_pct" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
    local metaspace_color="GREEN"
    if [ "$metaspace_int" -gt 90 ] 2>/dev/null; then
        metaspace_color="RED"
    elif [ "$metaspace_int" -gt 80 ] 2>/dev/null; then
        metaspace_color="YELLOW"
    fi
    print_metric "Usage" "${metaspace_pct}%" "" "$metaspace_color"
    
    # Compressed Class Space
    print_section "Compressed Class Space"
    local class_space_used=$(get_metric_with_tag "jvm.memory.used" "id" "Compressed Class Space")
    local class_space_max=$(get_metric_with_tag "jvm.memory.max" "id" "Compressed Class Space")
    
    if [ "$class_space_used" != "null" ]; then
        local class_space_pct=$(calculate_percentage "$class_space_used" "$class_space_max")
        print_metric "Used" "$(format_bytes "$class_space_used")" "" "YELLOW"
        print_metric "Max" "$(format_bytes "$class_space_max")" "" "CYAN"
        local class_space_int=$(echo "$class_space_pct" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
        local class_space_color="GREEN"
        if [ "$class_space_int" -gt 90 ] 2>/dev/null; then
            class_space_color="RED"
        elif [ "$class_space_int" -gt 80 ] 2>/dev/null; then
            class_space_color="YELLOW"
        fi
        print_metric "Usage" "${class_space_pct}%" "" "$class_space_color"
    fi
    
    # Code Cache
    print_section "Code Cache"
    local code_cache_used=$(get_metric_with_tag "jvm.memory.used" "id" "CodeCache")
    local code_cache_max=$(get_metric_with_tag "jvm.memory.max" "id" "CodeCache")
    
    if [ "$code_cache_used" != "null" ]; then
        local code_cache_pct=$(calculate_percentage "$code_cache_used" "$code_cache_max")
        print_metric "Used" "$(format_bytes "$code_cache_used")" "" "YELLOW"
        print_metric "Max" "$(format_bytes "$code_cache_max")" "" "CYAN"
        local code_cache_int=$(echo "$code_cache_pct" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
        local code_cache_color="GREEN"
        if [ "$code_cache_int" -gt 90 ] 2>/dev/null; then
            code_cache_color="RED"
        elif [ "$code_cache_int" -gt 80 ] 2>/dev/null; then
            code_cache_color="YELLOW"
        fi
        print_metric "Usage" "${code_cache_pct}%" "" "$code_cache_color"
    fi
    
    ###############################################################################
    # THREAD METRICS
    ###############################################################################
    print_header "THREAD METRICS"
    
    local threads_live=$(get_metric "jvm.threads.live")
    local threads_peak=$(get_metric "jvm.threads.peak")
    local threads_daemon=$(get_metric "jvm.threads.daemon")
    local threads_states=$(curl -s "${ACTUATOR_BASE}/metrics/jvm.threads.states" 2>/dev/null)
    
    print_metric "Live Threads" "${threads_live}" "" "YELLOW"
    print_metric "Peak Threads" "${threads_peak}" "" "CYAN"
    print_metric "Daemon Threads" "${threads_daemon}" "" "CYAN"
    
    # Thread States - Get states by querying with state tag (format: tag=state:runnable)
    print_section "Thread States"
    local thread_states_found=false
    for state in "runnable" "blocked" "waiting" "timed-waiting" "new" "terminated"; do
        local state_count=$(get_metric "jvm.threads.states" "tag=state:${state}")
        if [ "$state_count" != "null" ] && [ "$state_count" != "0" ]; then
            local state_display=$(echo "$state" | awk '{for(i=1;i<=NF;i++){ $i=toupper(substr($i,1,1)) substr($i,2) }}1' | sed 's/-/ /g')
            local state_int=$(echo "$state_count" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
            if [ "$state_int" -gt 0 ] 2>/dev/null; then
                echo -e "  ${CYAN}${state_display}: ${state_count}${NC}"
                thread_states_found=true
            fi
        fi
    done
    if [ "$thread_states_found" = false ]; then
        echo -e "  ${YELLOW}Thread state details not available${NC}"
    fi
    
    ###############################################################################
    # GARBAGE COLLECTION METRICS
    ###############################################################################
    print_header "GARBAGE COLLECTION METRICS"
    
    # GC Pause Time
    local gc_pause=$(curl -s "${ACTUATOR_BASE}/metrics/jvm.gc.pause" 2>/dev/null)
    if [ -n "$gc_pause" ] && echo "$gc_pause" | jq -e '.measurements' > /dev/null 2>&1; then
        print_section "GC Pause Times"
        echo "$gc_pause" | jq -r '.measurements[] | select(.statistic == "total") | "  \(.value / 1000000) ms"' | head -1 | while read line; do
            if [ -n "$line" ]; then
                print_metric "Total GC Pause" "${line}" "" "YELLOW"
            fi
        done
        
        echo "$gc_pause" | jq -r '.measurements[] | select(.statistic == "count") | "  Count: \(.value)"' | head -1 | while read line; do
            if [ -n "$line" ]; then
                echo -e "  ${CYAN}${line}${NC}"
            fi
        done
    else
        echo -e "  ${YELLOW}GC metrics not available (may require GC logging to be enabled)${NC}"
    fi
    
    # Memory Pool GC
    local gc_memory_allocated=$(get_metric "jvm.gc.memory.allocated")
    local gc_memory_promoted=$(get_metric "jvm.gc.memory.promoted")
    
    if [ "$gc_memory_allocated" != "null" ]; then
        print_section "GC Memory Operations"
        print_metric "Memory Allocated" "$(format_bytes "$gc_memory_allocated")" "" "CYAN"
    fi
    
    if [ "$gc_memory_promoted" != "null" ]; then
        print_metric "Memory Promoted" "$(format_bytes "$gc_memory_promoted")" "" "CYAN"
    fi
    
    ###############################################################################
    # CLASS LOADING METRICS
    ###############################################################################
    print_header "CLASS LOADING METRICS"
    
    local classes_loaded=$(get_metric "jvm.classes.loaded")
    local classes_unloaded=$(get_metric "jvm.classes.unloaded")
    
    print_metric "Loaded Classes" "${classes_loaded}" "" "YELLOW"
    print_metric "Unloaded Classes" "${classes_unloaded}" "" "CYAN"
    
    ###############################################################################
    # DATABASE CONNECTION POOL METRICS
    ###############################################################################
    print_header "DATABASE CONNECTION POOL (HikariCP)"
    
    local hikari_active=$(get_metric "hikari.connections.active")
    local hikari_idle=$(get_metric "hikari.connections.idle")
    local hikari_pending=$(get_metric "hikari.connections.pending")
    local hikari_timeout=$(get_metric "hikari.connections.timeout")
    local hikari_max=$(get_metric "hikari.connections.max")
    
    if [ "$hikari_active" != "null" ]; then
        print_metric "Active Connections" "${hikari_active}" "" "YELLOW"
    fi
    
    if [ "$hikari_idle" != "null" ]; then
        print_metric "Idle Connections" "${hikari_idle}" "" "GREEN"
    fi
    
    if [ "$hikari_pending" != "null" ]; then
        local pending_int=$(echo "$hikari_pending" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
        local pending_color="GREEN"
        if [ "$pending_int" -gt 0 ] 2>/dev/null; then
            pending_color="RED"
        fi
        print_metric "Pending Connections" "${hikari_pending}" "" "$pending_color"
    fi
    
    if [ "$hikari_timeout" != "null" ]; then
        local timeout_int=$(echo "$hikari_timeout" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
        if [ "$timeout_int" -gt 0 ] 2>/dev/null; then
            print_metric "Connection Timeouts" "${hikari_timeout}" "" "RED"
        fi
    fi
    
    if [ "$hikari_max" != "null" ]; then
        print_metric "Max Pool Size" "${hikari_max}" "" "CYAN"
        if [ "$hikari_active" != "null" ]; then
            local pool_usage_pct=$(calculate_percentage "$hikari_active" "$hikari_max")
            local pool_int=$(echo "$pool_usage_pct" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
            local pool_color="GREEN"
            if [ "$pool_int" -gt 80 ] 2>/dev/null; then
                pool_color="YELLOW"
            fi
            print_metric "Pool Usage" "${pool_usage_pct}%" "" "$pool_color"
        fi
    fi
    
    ###############################################################################
    # SYSTEM METRICS
    ###############################################################################
    print_header "SYSTEM METRICS"
    
    local cpu_usage=$(get_metric "system.cpu.usage")
    local cpu_count=$(get_metric "system.cpu.count")
    local process_cpu_usage=$(get_metric "process.cpu.usage")
    
    if [ "$cpu_usage" != "null" ]; then
        local cpu_pct=$(echo "$cpu_usage" | awk '{printf "%.1f", $1 * 100}')
        local cpu_int=$(echo "$cpu_pct" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
        local cpu_color="GREEN"
        if [ "$cpu_int" -gt 80 ] 2>/dev/null; then
            cpu_color="RED"
        fi
        print_metric "System CPU Usage" "${cpu_pct}%" "" "$cpu_color"
    fi
    
    if [ "$cpu_count" != "null" ]; then
        print_metric "CPU Cores" "${cpu_count}" "" "CYAN"
    fi
    
    if [ "$process_cpu_usage" != "null" ]; then
        local proc_cpu_pct=$(echo "$process_cpu_usage" | awk '{printf "%.1f", $1 * 100}')
        local proc_cpu_int=$(echo "$proc_cpu_pct" | awk '{printf "%.0f", $1}' 2>/dev/null || echo "0")
        local proc_cpu_color="YELLOW"
        if [ "$proc_cpu_int" -gt 80 ] 2>/dev/null; then
            proc_cpu_color="RED"
        fi
        print_metric "Process CPU Usage" "${proc_cpu_pct}%" "" "$proc_cpu_color"
    fi
    
    # Uptime
    local uptime_seconds=$(get_metric "process.uptime")
    if [ "$uptime_seconds" != "null" ]; then
        local uptime_days=$(echo "$uptime_seconds" | awk '{printf "%.1f", $1 / 86400}')
        local uptime_hours=$(echo "$uptime_seconds" | awk '{printf "%.1f", ($1 % 86400) / 3600}')
        print_metric "Uptime" "${uptime_days} days, ${uptime_hours} hours" "" "CYAN"
    fi
    
    ###############################################################################
    # SUMMARY & RECOMMENDATIONS
    ###############################################################################
    print_header "SUMMARY & HEALTH INDICATORS"
    
    local health_status="HEALTHY"
    local warnings=()
    local recommendations=()
    
    # Check heap usage (handle float comparison)
    if [ "$heap_usage_pct" != "N/A" ]; then
        local heap_int=$(echo "$heap_usage_pct" | awk '{printf "%.0f", $1}')
        if [ "$heap_int" -gt 80 ] 2>/dev/null; then
            health_status="WARNING"
            warnings+=("Heap usage is above 80% (${heap_usage_pct}%)")
            recommendations+=("Consider increasing heap size or optimizing memory usage")
        fi
    fi
    
    # Check metaspace (handle float comparison)
    if [ "$metaspace_pct" != "N/A" ]; then
        local metaspace_int=$(echo "$metaspace_pct" | awk '{printf "%.0f", $1}')
        if [ "$metaspace_int" -gt 90 ] 2>/dev/null; then
            health_status="WARNING"
            warnings+=("Metaspace usage is above 90% (${metaspace_pct}%)")
            recommendations+=("Consider increasing MaxMetaspaceSize or removing unused dependencies")
        fi
    fi
    
    # Check connection pool (handle float comparison)
    if [ "$hikari_pending" != "null" ]; then
        local pending_int=$(echo "$hikari_pending" | awk '{printf "%.0f", $1}')
        if [ "$pending_int" -gt 0 ] 2>/dev/null; then
            health_status="WARNING"
            warnings+=("Database connection pool has pending connections")
            recommendations+=("Consider increasing connection pool size")
        fi
    fi
    
    # Check thread count (handle float comparison)
    if [ "$threads_live" != "null" ]; then
        local threads_int=$(echo "$threads_live" | awk '{printf "%.0f", $1}')
        if [ "$threads_int" -gt 200 ] 2>/dev/null; then
            warnings+=("High thread count: ${threads_live} threads")
            recommendations+=("Consider optimizing thread pools (Undertow, task execution)")
        fi
    fi
    
    # Print status
    if [ "$health_status" = "HEALTHY" ]; then
        echo -e "  ${GREEN}✓ Status: ${health_status}${NC}"
    else
        echo -e "  ${YELLOW}⚠ Status: ${health_status}${NC}"
    fi
    
    # Print warnings
    if [ ${#warnings[@]} -gt 0 ]; then
        echo ""
        echo -e "  ${YELLOW}Warnings:${NC}"
        for warning in "${warnings[@]}"; do
            echo -e "    • ${warning}"
        done
    fi
    
    # Print recommendations
    if [ ${#recommendations[@]} -gt 0 ]; then
        echo ""
        echo -e "  ${CYAN}Recommendations:${NC}"
        for rec in "${recommendations[@]}"; do
            echo -e "    • ${rec}"
        done
    fi
    
    # Footer
    echo ""
    echo -e "${BOLD}${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}Report generated at: $(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo ""
}

# Run main function
main "$@"
