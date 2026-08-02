-- Tokens created before V15 were stored as plaintext and must not remain usable.
UPDATE Accounts
SET Reset_Token = NULL,
    Reset_Token_Expires_At = NULL
WHERE Reset_Token IS NOT NULL;
