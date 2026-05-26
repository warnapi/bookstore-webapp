<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Мои заказы");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-receipt"></i> Мои заказы</h1>

<c:choose>
    <c:when test="${empty orders}">
        <div class="alert alert-info">
            <p class="mb-0">Вы ещё не делали заказов.</p>
        </div>
        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary">
            <i class="bi bi-bag"></i> Перейти в каталог
        </a>
    </c:when>
    <c:otherwise>
        <div class="table-responsive">
            <table class="table table-hover">
                <thead>
                    <tr>
                        <th>№ заказа</th>
                        <th>Дата</th>
                        <th>Сумма</th>
                        <th>Статус</th>
                        <th>Действия</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="order" items="${orders}">
                        <tr>
                            <td><strong>${order.orderNumber}</strong></td>
                            <td>${order.createdAt}</td>
                            <td><strong>${order.totalAmount} BYN</strong></td>
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
                            <td>
                                <a href="${pageContext.request.contextPath}/order/details/${order.id}" class="btn btn-sm btn-outline-primary">
                                    <i class="bi bi-eye"></i>
                                </a>
                                <c:if test="${order.canBeCancelled()}">
                                    <form action="${pageContext.request.contextPath}/user/orders" method="post" class="d-inline">
                                        <input type="hidden" name="action" value="cancelOrder">
                                        <input type="hidden" name="orderId" value="${order.id}">
                                        <button type="submit" class="btn btn-sm btn-outline-danger" 
                                                onclick="return confirm('Отменить заказ?')">
                                            <i class="bi bi-x"></i>
                                        </button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        
        <!-- Пагинация -->
        <c:if test="${totalPages > 1}">
            <nav aria-label="Пагинация">
                <ul class="pagination justify-content-center">
                    <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/user/orders?page=${currentPage - 1}">
                            Назад
                        </a>
                    </li>
                    <c:forEach var="i" begin="1" end="${totalPages}">
                        <li class="page-item ${i == currentPage ? 'active' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/user/orders?page=${i}">
                                ${i}
                            </a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                        <a class="page-link" href="${pageContext.request.contextPath}/user/orders?page=${currentPage + 1}">
                            Вперёд
                        </a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </c:otherwise>
</c:choose>
