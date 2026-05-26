<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Детали заказа");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-receipt"></i> Детали заказа <strong>${order.orderNumber}</strong></h1>

<div class="row">
    <div class="col-lg-8">
        <div class="card mb-4">
            <div class="card-header">
                <h5>Информация о заказе</h5>
            </div>
            <div class="card-body">
                <table class="table table-borderless">
                    <tr>
                        <th>Номер заказа:</th>
                        <td><strong>${order.orderNumber}</strong></td>
                    </tr>
                    <tr>
                        <th>Дата размещения:</th>
                        <td>${order.createdAt}</td>
                    </tr>
                    <tr>
                        <th>Статус:</th>
                        <td>
                            <c:choose>
                                <c:when test="${order.status.name() == 'PENDING'}">
                                    <span class="badge bg-warning">В ожидании</span>
                                </c:when>
                                <c:when test="${order.status.name() == 'PAID'}">
                                    <span class="badge bg-info">Оплачен</span>
                                </c:when>
                                <c:when test="${order.status.name() == 'SHIPPED'}">
                                    <span class="badge bg-primary">Отправлен</span>
                                </c:when>
                                <c:when test="${order.status.name() == 'DELIVERED'}">
                                    <span class="badge bg-success">Доставлен</span>
                                </c:when>
                                <c:when test="${order.status.name() == 'CANCELLED'}">
                                    <span class="badge bg-danger">Отменён</span>
                                </c:when>
                            </c:choose>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">
                <h5>Товары в заказе</h5>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${empty orderItems}">
                        <p class="text-muted">Товары не найдены</p>
                    </c:when>
                    <c:otherwise>
                        <div class="list-group">
                            <c:forEach var="item" items="${orderItems}">
                                <div class="list-group-item">
                                    <div class="row align-items-center">
                                        <div class="col-md-8">
                                            <h6 class="mb-1">${item.bookTitle}</h6>
                                            <p class="mb-1 text-muted">${item.bookAuthor}</p>
                                            <small>${item.quantity} шт. × ${item.price} BYN</small>
                                        </div>
                                        <div class="col-md-4 text-end">
                                            <strong>${item.totalPrice} BYN</strong>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    
    <div class="col-lg-4">
        <div class="card">
            <div class="card-body">
                <h4>Итого</h4>
                <hr>
                <p class="display-6">${order.totalAmount} BYN</p>
                
                <c:if test="${order.canBeCancelled()}">
                    <form action="${pageContext.request.contextPath}/user/orders" method="post" class="mt-3">
                        <input type="hidden" name="action" value="cancelOrder">
                        <input type="hidden" name="orderId" value="${order.id}">
                        <button type="submit" class="btn btn-danger w-100" 
                                onclick="return confirm('Отменить заказ?')">
                            <i class="bi bi-x"></i> Отменить заказ
                        </button>
                    </form>
                </c:if>
                
                <a href="${pageContext.request.contextPath}/user/orders" class="btn btn-outline-secondary w-100 mt-2">
                    <i class="bi bi-arrow-left"></i> Вернуться к списку заказов
                </a>
            </div>
        </div>
    </div>
</div>
