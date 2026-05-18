<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Заказ оформлен");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<div class="text-center py-5">
    <i class="bi bi-check-circle text-success" style="font-size: 4rem;"></i>
    <h1 class="mt-4">Спасибо за ваш заказ!</h1>
    
    <c:if test="${not empty order}">
        <div class="card mx-auto mt-4" style="max-width: 500px;">
            <div class="card-body">
                <h5>Номер заказа: <strong>${order.orderNumber}</strong></h5>
                <p>Сумма: <strong>${order.totalAmount} ₽</strong></p>
                <p>Статус: <span class="badge bg-primary">${order.status.name()}</span></p>
                <p>Дата: ${order.createdAt}</p>
            </div>
        </div>
    </c:if>
    
    <div class="mt-4">
        <a href="${pageContext.request.contextPath}/user/orders" class="btn btn-primary btn-lg me-2">
            <i class="bi bi-list"></i> Мои заказы
        </a>
        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-outline-primary btn-lg">
            <i class="bi bi-grid"></i> Продолжить покупки
        </a>
    </div>
</div>
