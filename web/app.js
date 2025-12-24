// Smart Eye X App JS - semua fitur UI aktif
document.addEventListener('DOMContentLoaded', () => {
    const bootScreen = document.querySelector('.boot-screen');
    const screens = document.querySelectorAll('.screen');
    setTimeout(() => {
        bootScreen.style.display = 'none';
        if (screens[0]) screens[0].classList.add('active');
    }, 3500);

    // Bottom navigation
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach((item, idx) => {
        item.addEventListener('click', () => {
            navItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            screens.forEach(s => s.classList.remove('active'));
            if (screens[idx]) screens[idx].classList.add('active');
        });
    });

    // Mode cards
    const modeCards = document.querySelectorAll('.mode-card');
    modeCards.forEach(card => {
        card.addEventListener('click', () => {
            modeCards.forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            console.log(`Mode aktif: ${card.querySelector('.mode-title').textContent}`);
        });
    });

    // Toggle switches
    const toggles = document.querySelectorAll('.toggle-switch');
    toggles.forEach(toggle => {
        toggle.addEventListener('click', () => toggle.classList.toggle('active'));
    });

    // AI Orb interaktif
    const aiOrb = document.querySelector('.ai-orb-inner');
    if (aiOrb) aiOrb.addEventListener('click', () => alert('AI Orb: Halo, Bung X!'));

    // Quick actions
    const actionBtns = document.querySelectorAll('.action-btn');
    actionBtns.forEach(btn => {
        btn.addEventListener('click', () => alert(`Action: ${btn.querySelector('.action-label').textContent}`));
    });

    // Face cards dummy
    const faceCards = document.querySelectorAll('.face-card');
    faceCards.forEach(card => {
        card.addEventListener('click', () => {
            const name = card.querySelector('.face-name').textContent;
            alert(`Mengenali wajah: ${name} (confidence 95%)`);
        });
    });

    // Battery update dummy
    const batteryValue = document.querySelectorAll('.battery-value');
    batteryValue.forEach(val => val.textContent = `${Math.floor(Math.random()*100)}%`);

    // Voice wave animation
    const waveBars = document.querySelectorAll('.wave-bar');
    setInterval(() => waveBars.forEach(bar => bar.style.height = `${20 + Math.random() * 35}px`), 400);

    // Dual battery animation
    const batteryLiquids = document.querySelectorAll('.battery-liquid');
    batteryLiquids.forEach(liquid => {
        setInterval(() => {
            const percent = Math.floor(Math.random() * 100);
            liquid.style.height = `${percent}%`;
        }, 2000);
    });

    // AI hybrid load bars
    const localLoad = document.querySelector('.local-load');
    const cloudLoad = document.querySelector('.cloud-load');
    if(localLoad && cloudLoad) {
        setInterval(() => {
            const localPercent = Math.floor(Math.random() * 100);
            const cloudPercent = Math.floor(Math.random() * 100);
            localLoad.style.width = localPercent + '%';
            cloudLoad.style.width = cloudPercent + '%';
        }, 1500);
    }
});
