# foodcountry-ktor

1. Start database
```
docker volume create pgsql_foodcountry_data
docker run -m 512m --name foodcountry_db -e POSTGRES_PASSWORD=foodcountry -e POSTGRES_USER=foodcountry -e POSTGRES_DB=foodcountry -v pgsql_foodcountry_data:/var/lib/postgresql/data --restart unless-stopped -p 5432:5432/tcp -d postgres:11.3-alpine
```
2. Start server
```
./gradlew run
```
3. Check server
```
curl --location --request POST "http://127.0.0.1:8080/public/user/auth" \
  --header "Content-Type: application/json" \
  --data "{
	\"username\":\"SYSTEM\",
	\"password\":\"qwerty1989\"
}"
```
response 200-OK:
```
{
	"authToken": "eyJhbGciOiJIUzUxMiJ9.eyJleHBpcmF0aW9uRGF0ZSI6MTU1NzI0MDY4MzE1NiwiaWQiOjEsImNyZWRlbnRpYWxzIjoiU1lTVEVNIiwic3ViIjoiZjI1OTViMGMtNmQ1ZC00OWExLWIyOGMtZTAyZTFhNmI2YzQ1IiwiaWF0IjoxNTU3MjQwNjIzfQ.VYBnKDal0EBCHP9oQw6uUsWqKL2fHEoeraxlf_iD2y6gxU4Hc37oN5EIHsJX9Jnyj2vPqWF4ZyouPPWsuF6Gyg",
	"refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJleHBpcmF0aW9uRGF0ZSI6MTU1NzI0MDY4MzE1MCwiaWQiOiJyZWZyZXNoIiwiY3JlZGVudGlhbHMiOiJyZWZyZXNoIiwic3ViIjoiZjI1OTViMGMtNmQ1ZC00OWExLWIyOGMtZTAyZTFhNmI2YzQ1IiwiaWF0IjoxNTU3MjQwNjIzfQ.1mkWFxJdvFX-Ak7vvrfFFGYdKxgNpcN__zDg-IxHKaa7f7o7VWUBvS9cwgxIgHQgP6I5ABANHi51NkuKa-7WBw",
	"user": {
		"id": 1,
		"name": "SYSTEM",
		"roles": ["SYSTEM"]
	}
}
```
