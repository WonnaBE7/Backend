<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>월별 카테고리 소비 분석</title>
</head>
<body>
<h1>${yearMonth} 카테고리별 소비</h1>
<table border="1" cellpadding="5">
    <tr>
        <th>카테고리</th>
        <th>소비 금액</th>
        <th>비율 (%)</th>
    </tr>
    <c:forEach var="cat" items="${categories}">
        <tr>
            <td>${cat.consumptionCategory}</td>
            <td>${cat.amount}</td>
            <td>${cat.percentage}</td>
        </tr>
    </c:forEach>
</table>
</body>
</html>
