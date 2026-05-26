<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Управление заказами");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-receipt"></i> Управление заказами</h1>

<div class="table-responsive">
    <table class="table table-hover">
        <thead>
            <tr>
                <th>№ заказа</th>
                <th>Пользователь</th>
                <th>Сумма</th>
                <th>Статус</th>
                <th>Дата</th>
                <th>Действия</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="order" items="${orders}">
                <tr>
                    <td><strong>${order.orderNumber}</strong></td>
                    <td>${order.userId}</td>
                    <td>${order.totalAmount} BYN</td>
                    <td>
                        <span class="badge bg-${order.status.name() == 'PAID' || order.status.name() == 'DELIVERED' ? 'success' : 
                                    order.status.name() == 'PENDING' ? 'warning' : 
                                    order.status.name() == 'CANCELLED' ? 'danger' : 'secondary'}">
                            ${order.status.name()}
                        </span>
                    </td>
                    <td>${order.createdAt}</td>
                    <td>
                        <a href="${pageContext.request.contextPath}/admin/orders?action=view&id=${order.id}" class="btn btn-sm btn-outline-primary">
                            <i class="bi bi-eye"></i>
                        </a>
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
                <a class="page-link" href="${pageContext.request.contextPath}/admin/orders?page=${currentPage - 1}">
                    Назад
                </a>
            </li>
            <c:forEach var="i" begin="1" end="${totalPages}">
                <li class="page-item ${i == currentPage ? 'active' : ''}">
                    <a class="page-link" href="${pageContext.request.contextPath}/admin/orders?page=${i}">
                        ${i}
                    </a>
                </li>
            </c:forEach>
            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                <a class="page-link" href="${pageContext.request.contextPath}/admin/orders?page=${currentPage + 1}">
                    Вперёд
                </a>
            </li>
        </ul>
    </nav>
</c:if>
