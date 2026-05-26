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
                        <th>ID пользователя:</th>
                        <td>${order.userId}</td>
                    </tr>
                    <tr>
                        <th>Дата размещения:</th>
                        <td>${order.createdAt}</td>
                    </tr>
                    <tr>
                        <th>Статус:</th>
                        <td>
                            <span class="badge bg-${order.status.name() == 'PAID' || order.status.name() == 'DELIVERED' ? 'success' : 
                                        order.status.name() == 'PENDING' ? 'warning' : 
                                        order.status.name() == 'CANCELLED' ? 'danger' : 'secondary'}">
                                ${order.status.name()}
                            </span>
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
                    <c:when test="${empty items}">
                        <p class="text-muted">Товары не найдены</p>
                    </c:when>
                    <c:otherwise>
                        <div class="list-group">
                            <c:forEach var="item" items="${items}">
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
                
                <form action="${pageContext.request.contextPath}/admin/orders" method="post">
                    <input type="hidden" name="action" value="updateStatus">
                    <input type="hidden" name="orderId" value="${order.id}">
                    <div class="mb-3">
                        <label class="form-label">Изменить статус</label>
                        <select name="status" class="form-select">
                            <option value="PENDING" ${order.status.name() == 'PENDING' ? 'selected' : ''}>В ожидании</option>
                            <option value="PAID" ${order.status.name() == 'PAID' ? 'selected' : ''}>Оплачен</option>
                            <option value="SHIPPED" ${order.status.name() == 'SHIPPED' ? 'selected' : ''}>Отправлен</option>
                            <option value="DELIVERED" ${order.status.name() == 'DELIVERED' ? 'selected' : ''}>Доставлен</option>
                            <option value="CANCELLED" ${order.status.name() == 'CANCELLED' ? 'selected' : ''}>Отменён</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">
                        <i class="bi bi-check"></i> Обновить статус
                    </button>
                </form>
                
                <a href="${pageContext.request.contextPath}/admin/orders" class="btn btn-outline-secondary w-100 mt-2">
                    <i class="bi bi-arrow-left"></i> Вернуться к списку
                </a>
            </div>
        </div>
    </div>
</div>
