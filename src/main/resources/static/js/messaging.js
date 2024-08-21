$(document).ready(function () {
    // Handle form submission for sending messages
    $('#messageForm').on('submit', function (e) {
        e.preventDefault();

        // Get the message content
        const messageContent = $('#messageContent').val().trim();

        if (messageContent !== "") {
            // Create message HTML
            const messageHtml = `
                <div class="d-flex justify-content-end mb-4">
                    <div class="msg_cotainer_send">
                        ${messageContent}
                        <span class="msg_time_send">${getCurrentTime()}</span>
                    </div>
                    <div class="img_cont_msg">
                        <img src="https://via.placeholder.com/50" class="rounded-circle user_img_msg" alt="">
                    </div>
                </div>`;

            // Append the new message to the messages container
            $('.messages').append(messageHtml);

            // Clear the input field after sending
            $('#messageContent').val('');
        }
    });

    // Function to get current time in HH:MM AM/PM format
    function getCurrentTime() {
        const now = new Date();
        let hours = now.getHours();
        const minutes = now.getMinutes();
        const ampm = hours >= 12 ? 'PM' : 'AM';
        hours = hours % 12;
        hours = hours ? hours : 12; // the hour '0' should be '12'
        const minutesFormatted = minutes < 10 ? '0' + minutes : minutes;
        return `${hours}:${minutesFormatted} ${ampm}`;
    }
});
