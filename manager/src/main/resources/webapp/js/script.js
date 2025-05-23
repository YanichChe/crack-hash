const tabButtons = document.querySelectorAll('.tab-button');
const tabContents = document.querySelectorAll('.tab-content');
const sendHashButton = document.getElementById('sendHash');
const sendCodeButton = document.getElementById('sendCode');
const sendEncodeButton = document.getElementById('sendEncode');
const hashResult = document.getElementById('hashResult');
const codeResult = document.getElementById('codeResult');
const encodeResult = document.getElementById('encodeResult');

tabButtons.forEach(button => {
    button.addEventListener('click', () => {
        const targetTab = button.dataset.tab;

        tabButtons.forEach(btn => btn.classList.remove('active'));
        tabContents.forEach(content => content.classList.remove('active'));

        button.classList.add('active');
        document.getElementById(targetTab).classList.add('active');
    });
});

sendHashButton.addEventListener('click', async () => {
    const hash = document.getElementById('hash').value;
    const maxLength = document.getElementById('maxLength').value;

    if (!hash || !maxLength) {
        hashResult.textContent = 'Please fill in both fields.';
        return;
    }

    try {
        const response = await fetch('/api/hash-crack', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ hash, maxLength })
        });

        if (response.ok) {
            hashResult.textContent = `Response: ${await response.text()}`;
        } else {
            hashResult.textContent = `Error: ${await response.text()}`;
        }
    } catch (error) {
        hashResult.textContent = 'Error: Network issue.';
    }
});

sendCodeButton.addEventListener('click', async () => {
    const code = document.getElementById('code').value;

    if (!code) {
        codeResult.textContent = 'Please enter a code.';
        return;
    }

    try {
        const response = await fetch(`/api/hash-crack/status/${code}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' },
        });

        if (response.ok) {
            codeResult.textContent = `Result: ${await response.text()}`;
        } else {
            codeResult.textContent = `Error: ${await response.text()}`;
        }
    } catch (error) {
        codeResult.textContent = 'Error: Network issue.';
    }
});

sendEncodeButton.addEventListener('click', async () => {
    const encodeString = document.getElementById('encode').value;

    if (!encodeString) {
        encodeResult.textContent = 'Please enter a text';
        return;
    }

    try {
        const response = await fetch(`/api/hash-crack/encode`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ encodeString })
        });

        if (response.ok) {
            encodeResult.textContent = `Result: ${await response.text()}`;
        } else {
            encodeResult.textContent = `Error: ${await response.text()}`;
        }
    } catch (error) {
        encodeResult.textContent = 'Error: Network issue.';
    }
});