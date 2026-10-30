const fs = require('node:fs');
const root = 'C:/dev_FE/climbers-hub/src/views/';
const templates = {
  Login: `<template>
  <AuthLayout eyebrow="WELCOME BACK" title="다시 만나 반가워요." description="오늘도 한 홀드 더. 당신의 다음 도전을 함께할게요.">
    <div v-if="errorMessage" class="hub-notice" role="alert">{{ errorMessage }}</div>
    <form class="hub-form" @submit.prevent="login">
      <div class="hub-field">
        <label for="login-id">아이디</label>
        <input id="login-id" v-model="loginId" placeholder="아이디를 입력해주세요" autocomplete="username" maxlength="50" required :disabled="pending" autocapitalize="none" spellcheck="false" />
      </div>
      <div class="hub-field">
        <label for="password">비밀번호</label>
        <div class="password-wrap">
          <input id="password" v-model="password" placeholder="비밀번호를 입력해주세요" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" maxlength="72" required :disabled="pending" />
          <button class="password-toggle" type="button" :aria-pressed="showPassword" :aria-label="showPassword ? '비밀번호 숨기기' : '비밀번호 보기'" :disabled="pending" @click="showPassword = !showPassword">{{ showPassword ? '숨기기' : '보기' }}</button>
        </div>
      </div>
      <button class="hub-submit" type="submit" :disabled="pending"><span>{{ pending ? '로그인 중…' : '로그인' }}</span><span v-if="!pending" class="arrow" aria-hidden="true">↗</span></button>
    </form>
    <p class="auth-switch">아직 함께하지 않으셨나요? <RouterLink to="/signup">회원가입</RouterLink></p>
  </AuthLayout>
</template>
`,
  Signup: `<template>
  <AuthLayout eyebrow="JOIN THE COMMUNITY" title="첫 홀드, 함께 잡아요." description="나만의 계정을 만들고 클라이밍 여정을 시작하세요.">
    <template v-if="success">
      <div class="success-mark" aria-hidden="true">✓</div>
      <div class="hub-notice success" role="status">가입이 완료되었어요!<br>등록한 아이디와 비밀번호로 로그인해주세요.</div>
      <RouterLink class="hub-submit text-decoration-none" to="/login">로그인하러 가기 <span class="arrow" aria-hidden="true">↗</span></RouterLink>
    </template>
    <template v-else>
      <div v-if="errorMessage" class="hub-notice" role="alert">{{ errorMessage }}</div>
      <form class="hub-form signup-form" @submit.prevent="signup">
        <fieldset class="signup-fields" :disabled="pending">
          <div class="hub-field">
            <label for="signup-id">아이디</label>
            <input id="signup-id" v-model="form.loginId" placeholder="나만의 아이디" autocomplete="username" pattern="[a-z0-9_]{4,50}" minlength="4" maxlength="50" required aria-describedby="id-help" autocapitalize="none" spellcheck="false" />
            <small id="id-help">영문 소문자·숫자·밑줄 조합, 4~50자</small>
          </div>
          <div class="hub-field">
            <label for="signup-name">이름 / 닉네임</label>
            <input id="signup-name" v-model="form.name" placeholder="어떻게 불러드릴까요?" autocomplete="nickname" maxlength="50" required />
          </div>
          <div class="hub-field">
            <label for="signup-email">이메일</label>
            <input id="signup-email" v-model="form.email" placeholder="hello@example.com" type="email" autocomplete="email" maxlength="255" required />
          </div>
          <div class="hub-field">
            <label for="signup-password">비밀번호</label>
            <div class="password-wrap">
              <input id="signup-password" v-model="form.password" placeholder="8자 이상 입력해주세요" :type="showPassword ? 'text' : 'password'" autocomplete="new-password" minlength="8" maxlength="72" required aria-describedby="password-help" />
              <button class="password-toggle" type="button" :aria-pressed="showPassword" :aria-label="showPassword ? '비밀번호 숨기기' : '비밀번호 보기'" @click="showPassword = !showPassword">{{ showPassword ? '숨기기' : '보기' }}</button>
            </div>
            <small id="password-help">8자 이상, 한글 등 다국어 포함 시 최대 72바이트</small>
          </div>
          <button class="hub-submit" type="submit"><span>{{ pending ? '가입 중…' : '함께 시작하기' }}</span><span v-if="!pending" class="arrow" aria-hidden="true">↗</span></button>
        </fieldset>
      </form>
      <p class="auth-switch">이미 계정이 있으신가요? <RouterLink to="/login">로그인</RouterLink></p>
    </template>
  </AuthLayout>
</template>
`,
  Home: `<template>
  <AuthLayout eyebrow="YOUR CLIMBING JOURNEY" :title="(auth.member?.name ?? '클라이머') + '님, 환영해요.'" description="서두르지 않아도 괜찮아요. 오늘도 나만의 페이스로.">
    <span class="profile-badge"><span class="status-dot"></span> 로그인되어 있어요</span>
    <section class="profile-card" aria-label="내 회원 정보">
      <div class="profile-top">
        <span class="profile-avatar" aria-hidden="true">{{ Array.from(auth.member?.name ?? 'C')[0] }}</span>
        <div><strong>{{ auth.member?.name }}</strong><small>CLIMBERS’ HUB MEMBER</small></div>
      </div>
      <dl class="profile-details"><dt>아이디</dt><dd>{{ auth.member?.loginId }}</dd><dt>이메일</dt><dd>{{ auth.member?.email }}</dd></dl>
    </section>
    <div v-if="errorMessage" class="hub-notice" role="alert">{{ errorMessage }}</div>
    <button class="hub-submit hub-logout" :disabled="pending" @click="logout">{{ pending ? '로그아웃 중…' : '로그아웃' }} <span class="arrow" aria-hidden="true">↗</span></button>
    <p class="home-note">작은 도전이 모여, 더 높은 곳으로.</p>
  </AuthLayout>
</template>
`,
};
for (const [name, template] of Object.entries(templates)) {
  const file = root + name + '.vue';
  let source = fs.readFileSync(file, 'utf8');
  source = source.slice(0, source.indexOf('<template>'));
  source = source.replace("import { useAuthStore } from '../stores/auth'", "import { useAuthStore } from '../stores/auth'\nimport AuthLayout from '../components/AuthLayout.vue'");
  if (name !== 'Home') source = source.replace("const pending = ref(false)", "const showPassword = ref(false)\nconst pending = ref(false)");
  fs.writeFileSync(file, source + template);
}
console.log('Updated auth page templates, preserving existing session and form logic.');
