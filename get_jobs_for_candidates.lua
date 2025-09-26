#!lua name=castlib
redis.register_function('get_jobs_for_candidate', function(keys, args)
    local candidateId = args[1]
    local event = args[2]
    local key = string.format("c:%s:e:%s:jobs", candidateId, event)
    return redis.call('SMEMBERS', key)
end)