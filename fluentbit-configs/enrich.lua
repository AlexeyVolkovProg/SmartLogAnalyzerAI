function enrich_linux_log(tag, timestamp, record)
    local msg = record["message"] or ""
    local msg_lower = string.lower(msg)
    local level = "INFO"

    if string.find(msg_lower, "failure") or string.find(msg_lower, "failed") or string.find(msg_lower, "error") then
        level = "ERROR"
    elseif string.find(msg_lower, "alert") then
        level = "WARN"
    elseif string.find(msg_lower, "check pass") or string.find(msg_lower, "invalid") then
        level = "WARN"
    end

    record["log_level"] = level
    return 1, timestamp, record
end

function add_service_name(tag, timestamp, record)
    if tag == "hadoop" then
        record["service_name"] = "hadoop"
    elseif tag == "linux" then
        record["service_name"] = "linux-server"
    else
        record["service_name"] = tag
    end
    return 1, timestamp, record
end
