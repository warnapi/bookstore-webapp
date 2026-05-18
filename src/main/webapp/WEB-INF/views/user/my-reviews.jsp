<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%
    request.setAttribute("title", "Мои отзывы");
%>
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4"><i class="bi bi-chat"></i> Мои отзывы</h1>

<c:choose>
    <c:when test="${empty reviews}">
        <div class="alert alert-info">
            <p class="mb-0">Вы ещё не оставляли отзывов.</p>
        </div>
        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary">
            <i class="bi bi-bag"></i> Перейти в каталог
        </a>
    </c:when>
    <c:otherwise>
        <div class="row">
            <c:forEach var="review" items="${reviews}">
                <div class="col-md-6 mb-4">
                    <div class="card h-100">
                        <div class="card-body">
                            <h5 class="card-title">${review.bookTitle}</h5>
                            <div class="mb-2">
                                <c:forEach var="i" begin="1" end="5">
                                    <i class="bi ${i <= review.rating ? 'bi-star-fill text-warning' : 'bi-star text-muted'}"></i>
                                </c:forEach>
                            </div>
                            <p class="card-text">${review.text}</p>
                            <p class="text-muted small">
                                Статус: 
                                <c:choose>
                                    <c:when test="${review.status.name() == 'APPROVED'}">
                                        <span class="badge bg-success">Одобрен</span>
                                    </c:when>
                                    <c:when test="${review.status.name() == 'PENDING_MODERATION'}">
                                        <span class="badge bg-warning">На модерации</span>
                                    </c:when>
                                    <c:when test="${review.status.name() == 'HIDDEN'}">
                                        <span class="badge bg-secondary">Скрыт</span>
                                    </c:when>
                                </c:choose>
                                <br>
                                ${review.createdAt}
                            </p>
                            <form action="${pageContext.request.contextPath}/review" method="post">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="reviewId" value="${review.id}">
                                <button type="submit" class="btn btn-sm btn-outline-danger" 
                                        onclick="return confirm('Удалить отзыв?')">
                                    <i class="bi bi-trash"></i> Удалить
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>
