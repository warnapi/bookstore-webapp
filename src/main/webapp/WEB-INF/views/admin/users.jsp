<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Управление пользователями");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-people"></i> Управление пользователями</h1>

<div class="table-responsive">
    <table class="table table-hover">
        <thead>
            <tr>
                <th>ID</th>
                <th>Email</th>
                <th>Имя</th>
                <th>Роль</th>
                <th>Дата регистрации</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="user" items="${users}">
                <tr>
                    <td>${user.id}</td>
                    <td>${user.email}</td>
                    <td>${user.name}</td>
                    <td>
                        <span class="badge bg-${user.role.name() == 'ADMIN' ? 'danger' : 'info'}">
                            ${user.role.name()}
                        </span>
                    </td>
                    <td>${user.createdAt}</td>
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
                <a class="page-link" href="${pageContext.request.contextPath}/admin/users?page=${currentPage - 1}">
                    Назад
                </a>
            </li>
            <c:forEach var="i" begin="1" end="${totalPages}">
                <li class="page-item ${i == currentPage ? 'active' : ''}">
                    <a class="page-link" href="${pageContext.request.contextPath}/admin/users?page=${i}">
                        ${i}
                    </a>
                </li>
            </c:forEach>
            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                <a class="page-link" href="${pageContext.request.contextPath}/admin/users?page=${currentPage + 1}">
                    Вперёд
                </a>
            </li>
        </ul>
    </nav>
</c:if>
