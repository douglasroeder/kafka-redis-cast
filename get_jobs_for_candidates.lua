#!lua name=castlib

redis.register_function('get_jobs_for_candidate', function(keys, args)
    local candidateId = args[1]
    local event = args[2]

    local key = string.format("c:%s:e:%s:jobs", candidateId, event)
    local jobIds = redis.call('SMEMBERS', key)

    local result = {
        candidateId = candidateId,
        event = event,
        jobIds = jobIds
    }

    -- Encode the table to JSON string (Redis 7+ supports it)
    return cjson.encode(result)
end)