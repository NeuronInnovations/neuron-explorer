SELECT
    d.id,
    d.account_id,
    d.devicekey,
    d.devicerole,
    d.devicetype,
    d.hederaaccountnumber,
    d.hederaparentaccountnumber,
    d.latitude,
    d.longitude,
    d.name,
    d.privatekey,
    d.publickey,
    d.registertime,
    d.registrationstatus,
    ARRAY_AGG(sr.service_id ORDER BY sr.id) AS service_ids,
    ARRAY_AGG(sr.fee ORDER BY sr.id) AS service_fees,
    t.error AS topic_error,
    t.stdin AS topic_stdin,
    t.stdout AS topic_stdout
FROM
    public.device AS d
LEFT JOIN
    public.serviceregister AS sr ON d.id = sr.device_id
LEFT JOIN
    public.topic AS t ON d.hederaaccountnumber = t.hederaaccountid
GROUP BY
    d.id, t.id;