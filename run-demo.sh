#!/bin/bash

echo "🔐 로그인 중..."
TOKEN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email": "rlagudeo@example.com", "password": "1234"}')

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

if [ -z "$ACCESS_TOKEN" ]; then
  echo "❌ 로그인 실패. 토큰을 가져올 수 없습니다."
  exit 1
fi

echo "✅ 토큰 발급 완료! 일부 토큰: ${ACCESS_TOKEN:0:20}..."

echo "🟢 Step 1: 새싹 투자형 진단"
curl -s -X POST "http://localhost:8080/api/nowme/diagnosis" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{"answers": [2,2,2,2,2,2,2,2,2,2,2,2]}'

echo -e "\n🟢 Step 2: 미래 준비형으로 변환"
curl -s -X POST "http://localhost:8080/api/nowme/diagnosis" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{"answers": [1,2,1,2,2,2,3,3,3,1,1,2]}'

echo -e "\n🟢 Step 3: wonnaBE 수정"
curl -s -X PATCH "http://localhost:8080/api/user/mypage/wonnabe" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{"selectedWonnabeIds": [2, 7, 11]}'

echo -e "\n🟢 Step 4: 최종 확인"
curl -s -X GET "http://localhost:8080/api/user/me" \
  -H "Authorization: Bearer $ACCESS_TOKEN"

echo -e "\n✅ 시연 완료!"
