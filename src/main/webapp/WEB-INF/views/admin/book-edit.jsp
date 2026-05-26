<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:set var="title" value="${not empty book ? 'Редактировать книгу' : 'Добавить книгу'}" />
<%@ include file="/WEB-INF/views/layout.jsp" %>

<h1 class="mb-4">
    <c:choose>
        <c:when test="${not empty book and book.id != null}">
            <i class="bi bi-pencil"></i> Редактировать книгу
        </c:when>
        <c:otherwise>
            <i class="bi bi-plus-circle"></i> Добавить книгу
        </c:otherwise>
    </c:choose>
</h1>

<div class="card">
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/admin/books" method="post">
            <input type="hidden" name="action" value="save">
            <c:if test="${not empty book and book.id != null}">
                <input type="hidden" name="id" value="${book.id}">
            </c:if>
            
            <div class="row">
                <div class="col-md-8">
                    <div class="mb-3">
                        <label for="title" class="form-label">Название *</label>
                        <input type="text" class="form-control" id="title" name="title" 
                               value="${book.title}" required>
                    </div>
                    
                    <div class="mb-3">
                        <label for="author" class="form-label">Автор *</label>
                        <input type="text" class="form-control" id="author" name="author" 
                               value="${book.author}" required>
                    </div>
                    
                    <div class="mb-3">
                        <label for="isbn" class="form-label">ISBN *</label>
                        <input type="text" class="form-control" id="isbn" name="isbn" 
                               value="${book.isbn}" required>
                    </div>
                    
                    <div class="mb-3">
                        <label for="description" class="form-label">Описание</label>
                        <textarea class="form-control" id="description" name="description" rows="5">${book.description}</textarea>
                    </div>
                    
                    <div class="mb-3">
                        <label for="category" class="form-label">Категория *</label>
                        <input type="text" class="form-control" id="category" name="category" 
                               value="${book.category}" required>
                    </div>
                </div>
                
                <div class="col-md-4">
                    <div class="mb-3">
                        <label for="price" class="form-label">Цена (BYN) *</label>
                        <input type="number" class="form-control" id="price" name="price" 
                               value="${book.price}" min="0" step="0.01" required>
                    </div>
                    
                    <div class="mb-3">
                        <label for="stock" class="form-label">Наличие на складе *</label>
                        <input type="number" class="form-control" id="stock" name="stock" 
                               value="${book.stock}" min="0" required>
                    </div>
                    
                    <div class="mb-3">
                        <label for="imageUrl" class="form-label">URL обложки</label>
                        <input type="url" class="form-control" id="imageUrl" name="imageUrl" 
                               value="${book.imageUrl}">
                    </div>
                    
                    <c:if test="${not empty book.imageUrl}">
                        <div class="mb-3">
                            <label class="form-label">Превью</label>
                            <img src="${book.imageUrl}" class="img-fluid" style="max-height: 300px;">
                        </div>
                    </c:if>
                </div>
            </div>
            
            <hr>
            
            <button type="submit" class="btn btn-primary">
                <i class="bi bi-check-circle"></i> Сохранить
            </button>
            <a href="${pageContext.request.contextPath}/admin/books" class="btn btn-secondary">
                <i class="bi bi-x-circle"></i> Отмена
            </a>
        </form>
    </div>
</div>
