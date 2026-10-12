# 実行: python3 verify-hs256.py
# JwtDemo.java が作ったHS256のトークンの署名を、Pythonの標準ライブラリで計算し直して、一致するかを確かめる
import base64, hashlib, hmac

token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbGljZSIsInJvbGUiOiJ1c2VyIiwiZXhwIjoxNzAwMDAwMzAwfQ.zBA5vAUWioM7619z3bvE6L6bN1AAnsLK2qH0161vuUA"
secret = b"demo-secret-key-for-experiment-only!"  # 実験用の仮の値

signing_input, signature = token.rsplit(".", 1)
expected = base64.urlsafe_b64encode(hmac.new(secret, signing_input.encode("ascii"), hashlib.sha256).digest()).rstrip(b"=").decode()
print("Javaが作った署名  :", signature)
print("Pythonで計算した値:", expected)
print("一致:", signature == expected)
