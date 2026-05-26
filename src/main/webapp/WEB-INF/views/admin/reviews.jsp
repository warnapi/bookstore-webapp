<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Модерация отзывов");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-chat"></i> Модерация отзывов</h1>

<div class="table-responsive">
    <table class="table table-hover">
        <thead>
            <tr>
                <th>Книга</th>
                <th>Пользователь</th>
                <th>Оценка</th>
                <th>Текст</th>
                <th>Статус</th>
                <th>Дата</th>
                <th>Действия</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="review" items="${reviews}">
                <tr>
                    <td>${review.bookTitle}</td>
                    <td>${review.userName}</td>
                    <td>
                        <c:forEach var="i" begin="1" end="5">
                            <i class="bi ${i <= review.rating ? 'bi-star-fill text-warning' : 'bi-star text-muted'}"></i>
                        </c:forEach>
                    </td>
                    <td>${review.text}</td>
                    <td>
                        <span class="badge bg-${review.status.name() == 'APPROVED' ? 'success' : 
                                    review.status.name() == 'PENDING_MODERATION' ? 'warning' : 'danger'}">
                            ${review.status.name()}
                        </span>
                    </td>
                    <td>${review.createdAt}</td>
                    <td>
                        <c:if test="${review.status.name() == 'PENDING_MODERATION'}">
                            <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline">
                                <input type="hidden" name="action" value="approve">
                                <input type="hidden" name="reviewId" value="${review.id}">
                                <button type="submit" class="btn btn-sm btn-success">
                                    <i class="bi bi-check"></i>
                                </button>
                            </form>
                            <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline">
                                <input type="hidden" name="action" value="hide">
                                <input type="hidden" name="reviewId" value="${review.id}">
                                <button type="submit" class="btn btn-sm btn-danger">
                                    <i class="bi bi-x"></i>
                                </button>
                            </form>
                        </c:if>
                        <c:if test="${review.status.name() != 'PENDING_MODERATION'}">
                            <form action="${pageContext.request.contextPath}/admin/reviews" method="post" class="d-inline">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="reviewId" value="${review.id}">
                                <button type="submit" class="btn btn-sm btn-outline-danger">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
