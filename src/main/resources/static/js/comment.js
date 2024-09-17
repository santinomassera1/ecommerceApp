document.addEventListener('DOMContentLoaded', function () {
    const commentForm = document.querySelector('.comments-section form');
    const commentContent = document.getElementById('content');

    commentForm.addEventListener('submit', function (event) {
        if (commentContent.value.trim() === '') {
            event.preventDefault();
            alert('El comentario no puede estar vacío.');
        }
    });
});