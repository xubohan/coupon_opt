# Gift REST API Contract

面向前端的赠送能力共有四个 REST 接口，统一前缀为 `/api/gifts`。所有响应均使用 `application/json`，错误场景沿用全局异常处理策略。

## 1. GET `/api/gifts/goods`
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
      "goodsId": 101,
      "goodsName": "高端组合",
      "type": "PORTFOLIO",
      "advisorId": 88
    }
  ]
}
```

## 2. POST `/api/gifts/candidates`
根据选中的商品计算可赠送的用户候选列表。

- **请求体**
```json
{
  "selectedGood": {
    "goodsId": 101,
    "goodsName": "高端组合",
    "type": "PORTFOLIO",
    "advisorId": 88
  },
  "allGoods": [
    {
      "goodsId": 101,
      "goodsName": "高端组合",
      "type": "PORTFOLIO",
      "advisorId": 88
    },
    {
      "goodsId": 202,
      "goodsName": "Pro套餐",
      "type": "PACKAGE",
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

## 3. POST `/api/gifts/check-eligibility`
按顺序校验一组用户是否满足赠送条件。
（当前接口仅保留占位，待外部校验接口确认后开放）

- **请求体**
```json
{
  "selectedGood": {
    "goodsId": 202,
    "goodsName": "Pro套餐",
    "type": "PACKAGE",
    "advisorId": 88
  },
  "userIds": [9001, 9002, 9003]
}
```

- **响应体** `200 OK`
```json
{
  "errorCode": 0,
  "errorMsg": "OK",
  "result": [true, false, true]
}
```
结果顺序与请求中 `userIds` 保持一致。

## 4. POST `/api/gifts/grant`
批量赠送。`source` 由后端生成，前端只需提交顾问信息及候选用户 ID。

- **请求体**
```json
{
  "selectedGood": {
    "goodsId": 202,
    "goodsName": "Pro套餐",
    "type": "PACKAGE",
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
Advisor {advisorId} granted product {goodsId, type} to user {userId} on {yyyy-MM-dd HH:mm:ss} (advisorId 取自 selectedGood.advisorId)
```

## 数据模型摘要
- `GoodsBaseVO`
  - `goodsId` *(Long)*
  - `goodsName` *(String)*
  - `type` *(String: `PORTFOLIO` / `PACKAGE`)*
  - `advisorId` *(Integer)*
- `GiftCandidateVO`
  - `userId` *(Integer)*
  - `nickName` *(String)*
  - `avatarUrl` *(String)*
  - `productName` *(String)*
  - `purchaseDate` *(String, yyyy-MM-dd HH:mm:ss)*
  - `durationMonths` *(Integer)*

所有举例仅用于说明结构，具体值以实际业务为准。
