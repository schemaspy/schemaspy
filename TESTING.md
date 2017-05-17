# Unit

# Integration Testing

## Test-containers
* Requires docker

### Oracle-XE
Since oracle jdbc driver is proprietary you need to fetch it on your own.

Put ojdbc[version].jar in ext-lib and it will be discovered.  
The OracleIT test will check for ojdbc* in ext-lib and also check 
if docker "works" if one of them fails the tests will be ignored.