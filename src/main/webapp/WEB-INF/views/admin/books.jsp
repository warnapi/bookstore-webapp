<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Управление книгами");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h1><i class="bi bi-book"></i> Управление книгами</h1>
    <a href="${pageContext.request.contextPath}/admin/books?action=add" class="btn btn-primary">
        <i class="bi bi-plus-circle"></i> Добавить книгу
    </a>
</div>

<div class="card">
    <div class="card-body">
        <table class="table table-hover">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Обложка</th>
                    <th>Название</th>
                    <th>Автор</th>
                    <th>ISBN</th>
                    <th>Цена</th>
                    <th>Наличие</th>
                    <th>Действия</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="book" items="${books}">
                    <tr>
                        <td>${book.id}</td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty book.imageUrl}">
                                    <img src="${book.imageUrl}" style="height: 50px; width: 35px; object-fit: cover;">
                                </c:when>
                                <c:otherwise>
                                    <i class="bi bi-book text-muted"></i>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>${book.title}</td>
                        <td>${book.author}</td>
                        <td>${book.isbn}</td>
                        <td>${book.price} BYN</td>
                        <td>
                            <span class="badge ${book.stock > 0 ? 'bg-success' : 'bg-danger'}">
                                ${book.stock}
                            </span>
                        </td>
                        <td>
                            <a href="${pageContext.request.contextPath}/admin/books?action=edit&id=${book.id}" class="btn btn-sm btn-outline-primary">
                                <i class="bi bi-pencil"></i>
                            </a>
                            <form action="${pageContext.request.contextPath}/admin/books" method="post" class="d-inline" onsubmit="return confirm('Удалить книгу?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${book.id}">
                                <button type="submit" class="btn btn-sm btn-outline-danger">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<!-- Пагинация -->
<c:if test="${totalPages > 1}">
    <nav class="mt-4">
        <ul class="pagination justify-content-center">
            <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                <a class="page-link" href="?page=${currentPage - 1}">Предыдущая</a>
            </li>
            <c:forEach var="i" begin="1" end="${totalPages}">
                <li class="page-item ${i == currentPage ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}">${i}</a>
                </li>
            </c:forEach>
            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                <a class="page-link" href="?page=${currentPage + 1}">Следующая</a>
            </li>
        </ul>
    </nav>
</c:if>
