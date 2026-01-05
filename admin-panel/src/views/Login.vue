<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 -->
    <div class="login-left">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon">🏭</div>
          <h1 class="brand-title">宋家纺织</h1>
          <p class="brand-slogan">专注功能家纺面料20年</p>
        </div>

        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">🏭</div>
            <div class="feature-text">
              <h3>自有工厂</h3>
              <p>2.5万㎡ 生产基地</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🧪</div>
            <div class="feature-text">
              <h3>专利认证</h3>
              <p>21件专利 · OEKO-TEX A类</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">🚚</div>
            <div class="feature-text">
              <h3>年出货量</h3>
              <p>3000万米面料产能</p>
            </div>
          </div>
        </div>

        <div class="copyright">
          © 2026 浙江宋家纺织 · 品质保证
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="login-right">
      <div class="login-form-wrapper">
        <div class="form-header">
          <h2 class="form-title">管理员登录</h2>
          <p class="form-subtitle">欢迎使用后台管理系统</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-button"
            >
              <span v-if="!loading">立即登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-tips">
          <el-alert
            title="测试账号：admin / admin123"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref(null)

// 加载状态
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  // 验证表单
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      const result = await userStore.login(loginForm.username, loginForm.password)

      if (result.success) {
        ElMessage.success('登录成功！欢迎回来~')

        // 跳转到目标页面或首页
        const redirect = route.query.redirect || '/'
        router.push(redirect)
      } else {
        ElMessage.error(result.message || '登录失败，请检查用户名和密码')
      }
    } catch (error) {
      console.error('登录错误:', error)
      ElMessage.error('登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ========== 左侧品牌展示区 ========== */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
}

/* 装饰性背景图案 */
.login-left::before {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  top: -200px;
  left: -200px;
  border-radius: 50%;
  animation: float 15s infinite ease-in-out;
}

.login-left::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
  bottom: -150px;
  right: -150px;
  border-radius: 50%;
  animation: float 20s infinite ease-in-out reverse;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-30px) scale(1.05);
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  color: white;
  text-align: center;
  max-width: 500px;
  animation: fadeInLeft 0.8s ease-out;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.brand-logo {
  margin-bottom: 60px;
}

.logo-icon {
  font-size: 80px;
  margin-bottom: 24px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

.brand-title {
  font-size: 48px;
  font-weight: 800;
  margin: 0 0 16px 0;
  letter-spacing: 4px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.brand-slogan {
  font-size: 18px;
  margin: 0;
  opacity: 0.95;
  font-weight: 300;
  letter-spacing: 2px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 60px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 20px 24px;
  border-radius: 16px;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(10px);
}

.feature-icon {
  font-size: 36px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.feature-text {
  text-align: left;
}

.feature-text h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 600;
}

.feature-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.copyright {
  font-size: 14px;
  opacity: 0.8;
}

/* ========== 右侧登录表单区 ========== */
.login-right {
  flex: 0 0 550px;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.05);
}

.login-form-wrapper {
  width: 100%;
  max-width: 420px;
  animation: fadeInRight 0.8s ease-out;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.form-header {
  margin-bottom: 40px;
}

.form-title {
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 12px 0;
}

.form-subtitle {
  font-size: 15px;
  color: #6B7280;
  margin: 0;
}

/* 表单样式 */
.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 14px 16px;
  background-color: #F9FAFB;
  border: 2px solid #E5E7EB;
  border-radius: 12px;
  transition: all 0.3s;
  box-shadow: none;
}

.login-form :deep(.el-input__wrapper:hover) {
  background-color: #F3F4F6;
  border-color: #D1D5DB;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background-color: #FFFFFF;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.login-button {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.login-tips {
  margin-top: 24px;
}

.login-tips :deep(.el-alert) {
  border-radius: 12px;
  border: none;
  background-color: #EFF6FF;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-left {
    display: none;
  }

  .login-right {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .login-right {
    padding: 30px 20px;
  }

  .form-title {
    font-size: 28px;
  }
}
</style>
