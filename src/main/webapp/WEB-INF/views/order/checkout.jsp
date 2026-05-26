<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Оформление заказа");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-receipt"></i> Оформление заказа</h1>

<c:choose>
    <c:when test="${hasUnavailableItems}">
        <div class="alert alert-warning">
            <i class="bi bi-exclamation-triangle"></i>
            <strong>Внимание!</strong> Некоторые товары в корзине недоступны. 
            Пожалуйста, удалите их перед оформлением заказа.
        </div>
    </c:when>
</c:choose>

<div class="row">
    <div class="col-lg-8">
        <div class="card">
            <div class="card-header">
                <h4>Сведения о заказе</h4>
            </div>
            <div class="card-body">
                <p>Пожалуйста, проверьте информацию о вашем заказе перед подтверждением.</p>
                <p>После подтверждения заказ будет обработан, и вы получите письмо с подтверждением.</p>
            </div>
        </div>
    </div>
    
    <div class="col-lg-4">
        <div class="card">
                    <div class="card-body">
                        <h4>Итого к оплате</h4>
                        <hr>
                        <p class="display-6">${cartTotal} BYN</p>
                
                <form action="${pageContext.request.contextPath}/order" method="post">
                    <input type="hidden" name="action" value="confirm">
                    <button type="submit" class="btn btn-success btn-lg w-100" ${hasUnavailableItems ? 'disabled' : ''}>
                        <i class="bi bi-check-circle"></i> Подтвердить заказ
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>
