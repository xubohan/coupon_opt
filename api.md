# Gift REST API Contract

面向前端的赠送能力共有3个 REST 接口，统一前缀为 `/coupon_gifts`。所有响应均使用 `application/json`，错误场景沿用全局异常处理策略。

## 1. GET `/v1/list_goods`
列出指定投顾当前可操作的商品（组合/套餐）。

- **Query 参数**
  - `advisorId` *(Integer, 必填)*：投顾 ID。

- **响应体** `200 OK`
```json
{
  "errorCode": 0,
  "errorMsg": "OK",
  "result": [
    {
      "productId": 101,
      "productName": "高端组合",
      "productType": "PORTFOLIO",
      "advisorId": 88
    }
  ]
}
```

## 2. POST `/v1_list_candidates`
根据选中的商品计算可赠送的用户候选列表。

- **请求体**
```json
{
  "selectedGood": {
    "productId": 101,
    "productName": "高端组合",
    "productType": "PORTFOLIO",
    "advisorId": 88
  },
  "allGoods": [
    {
      "productId": 101,
      "productName": "高端组合",
      "productType": "PORTFOLIO",
      "advisorId": 88
    },
    {
      "productId": 202,
      "productName": "Pro套餐",
      "productType": "PACKAGE",
      "advisorId": 88
    }
  ]
}
```

- **响应体** `200 OK`
```json
{
  "errorCode": 0,
  "errorMsg": "OK",
  "result": [
    {
      "userId": 9001,
      "nickName": "张三",
      "avatarUrl": "https://...",
      "productName": "Pro套餐",
      "purchaseDate": "2024-01-01 08:30:15",
      "durationMonths": 6
    }
  ]
}
```
前端可自行做分页或筛选。

## 3. POST `/v1/grant_product`
批量赠送。`source` 由后端生成，前端只需提交顾问信息及候选用户 ID。

- **请求体**
```json
{
  "selectedGood": {
    "productId": 202,
    "productName": "Pro套餐",
    "productType": "PACKAGE",
    "advisorId": 88
  },
  "attr": "period:30",
  "candidates": [9001, 9002]
}
```

- **响应体** `200 OK`
```json
{
  "errorCode": 0,
  "errorMsg": "OK",
  "result": [true, false]
}
```
数组元素代表各用户赠送结果，索引与 `candidates` 对应。服务器端会生成如下格式的 `source` 并逐个调用 Dubbo：

```
Advisor {advisorId} granted product {productId, type} to user {userId} on {yyyy-MM-dd HH:mm:ss} (advisorId 取自 selectedGood.advisorId)
```

## 数据模型摘要
- `GoodsBaseVO`
  - `productId` *(Integer)*
  - `productName` *(String)*
  - `productType` *(String: `PORTFOLIO` / `PACKAGE`)*
  - `advisorId` *(Integer)*
- `GiftCandidateVO`
  - `userId` *(Integer)*
  - `nickName` *(String)*
  - `avatarUrl` *(String)*
  - `productName` *(String)*
  - `purchaseDate` *(String, yyyy-MM-dd HH:mm:ss)*
  - `durationMonths` *(Integer)*

所有举例仅用于说明结构，具体值以实际业务为准。

