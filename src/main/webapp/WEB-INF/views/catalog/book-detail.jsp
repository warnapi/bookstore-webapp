<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="title" value="${not empty book ? book.title : 'Книга'}" />
<%@ include file="/WEB-INF/views/layout.jsp" %>

<div class="row">
    <div class="col-md-5">
        <div class="card">
            <c:choose>
                <c:when test="${not empty book.imageUrl}">
                    <img src="${book.imageUrl}" class="card-img-top" style="height: 400px; object-fit: cover;" alt="${book.title}">
                </c:when>
                <c:otherwise>
                    <div style="height: 400px; background: #f8f9fa; display: flex; align-items: center; justify-content: center;">
                        <i class="bi bi-book fs-1 text-muted"></i>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    
    <div class="col-md-7">
        <h1>${book.title}</h1>
        <h4 class="text-muted">${book.author}</h4>
        
        <c:if test="${book.averageRating > 0}">
            <div class="rating mb-3">
                <c:forEach var="i" begin="1" end="5">
                    <i class="bi ${i <= book.averageRating ? 'bi-star-fill' : 'bi-star'}"></i>
                </c:forEach>
                <span class="ms-2">${book.averageRating} (${book.reviewCount} отзывов)</span>
            </div>
        </c:if>
        
        <p class="price display-5 mb-4">${book.price} BYN</p>
        
        <div class="mb-4">
            <p><strong>ISBN:</strong> ${book.isbn}</p>
            <p><strong>Категория:</strong> ${book.category}</p>
            <p><strong>Наличие:</strong> 
                <c:choose>
                    <c:when test="${book.stock > 0}">
                        <span class="text-success">${book.stock} шт.</span>
                    </c:when>
                    <c:otherwise>
                        <span class="text-danger">Нет в наличии</span>
                    </c:otherwise>
                </c:choose>
            </p>
        </div>
        
        <c:if test="${book.stock > 0}">
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <form action="${pageContext.request.contextPath}/cart" method="post" class="d-flex gap-2">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="bookId" value="${book.id}">
                        <div class="input-group" style="max-width: 150px;">
                            <input type="number" name="quantity" class="form-control" value="1" min="1" max="${book.stock}">
                        </div>
                        <button type="submit" class="btn btn-primary btn-lg">
                            <i class="bi bi-cart"></i> Добавить в корзину
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="btn btn-primary btn-lg">
                        <i class="bi bi-box-arrow-in-right"></i> Войдите, чтобы купить
                    </a>
                </c:otherwise>
            </c:choose>
        </c:if>
        
        <hr class="my-4">
        
        <h5>Описание</h5>
        <p>${book.description != null ? book.description : 'Описание отсутствует'}</p>
    </div>
</div>

<!-- Отзывы -->
<hr class="my-5">
<h2>Отзывы (${reviews.size()})</h2>

<c:if test="${canReview}">
    <div class="card mb-4">
        <div class="card-body">
            <h5>Оставить отзыв</h5>
            <form action="${pageContext.request.contextPath}/review" method="post">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="bookId" value="${book.id}">
                <input type="hidden" name="bookTitle" value="${book.title}">
                
                <div class="mb-3">
                    <label class="form-label">Оценка</label>
                    <select name="rating" class="form-select" required>
                        <option value="5">5 - Отлично</option>
                        <option value="4">4 - Хорошо</option>
                        <option value="3">3 - Нормально</option>
                        <option value="2">2 - Плохо</option>
                        <option value="1">1 - Ужасно</option>
                    </select>
                </div>
                
                <div class="mb-3">
                    <label for="text" class="form-label">Текст отзыва</label>
                    <textarea name="text" id="text" class="form-control" rows="4" required></textarea>
                </div>
                
                <button type="submit" class="btn btn-primary">
                    Отправить отзыв
                </button>
            </form>
        </div>
    </div>
</c:if>

<c:choose>
    <c:when test="${not empty reviews}">
        <c:forEach var="review" items="${reviews}">
            <div class="card mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between">
                        <h5 class="mb-1">${review.userName}</h5>
                        <div class="rating">
                            <c:forEach var="i" begin="1" end="5">
                                <i class="bi ${i <= review.rating ? 'bi-star-fill' : 'bi-star'}"></i>
                            </c:forEach>
                        </div>
                    </div>
                    <small class="text-muted">${review.createdAt}</small>
                    <p class="mt-2">${review.text}</p>
                </div>
            </div>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <p class="text-muted">Отзывов пока нет. Будьте первым!</p>
    </c:otherwise>
</c:choose>
