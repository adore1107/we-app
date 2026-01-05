<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo-container">
        <transition name="logo-fade">
          <div v-if="!isCollapse" class="logo-full">
            <span class="logo-icon">🏭</span>
            <div class="logo-text">
              <div class="logo-title">宋家纺织</div>
              <div class="logo-subtitle">管理后台</div>
            </div>
          </div>
          <div v-else class="logo-collapsed">
            <span class="logo-icon-small">🏭</span>
          </div>
        </transition>
      </div>

      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          background-color="#1F2937"
          text-color="#9CA3AF"
          active-text-color="#FFFFFF"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/home" class="menu-item">
            <el-icon><HomeFilled /></el-icon>
            <template #title>
              <span class="menu-title">首页</span>
            </template>
          </el-menu-item>

          <el-sub-menu index="product" class="menu-submenu">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span class="menu-title">商品管理</span>
            </template>
            <el-menu-item index="/product/list">商品列表</el-menu-item>
            <el-menu-item index="/product/add">添加商品</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/category/list" class="menu-item">
            <el-icon><Menu /></el-icon>
            <template #title>
              <span class="menu-title">分类管理</span>
            </template>
          </el-menu-item>

          <el-menu-item index="/banner/list" class="menu-item">
            <el-icon><Picture /></el-icon>
            <template #title>
              <span class="menu-title">轮播图管理</span>
            </template>
          </el-menu-item>

          <el-menu-item index="/user/list" class="menu-item">
            <el-icon><User /></el-icon>
            <template #title>
              <span class="menu-title">用户管理</span>
            </template>
          </el-menu-item>

          <el-menu-item index="/comment/list" class="menu-item">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>
              <span class="menu-title">评论管理</span>
            </template>
          </el-menu-item>

          <el-menu-item index="/favorite/list" class="menu-item">
            <el-icon><Star /></el-icon>
            <template #title>
              <span class="menu-title">收藏管理</span>
            </template>
          </el-menu-item>

          <el-menu-item index="/analytics" class="menu-item">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>
              <span class="menu-title">数据分析</span>
            </template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>

          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute">{{ currentRoute }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-tooltip content="刷新页面" placement="bottom">
            <el-icon class="header-icon" @click="refreshPage">
              <Refresh />
            </el-icon>
          </el-tooltip>

          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon" @click="toggleFullscreen">
              <FullScreen />
            </el-icon>
          </el-tooltip>

          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="36" class="user-avatar">
                {{ userStore.realName ? userStore.realName.charAt(0) : 'A' }}
              </el-avatar>
              <span class="user-name">{{ userStore.realName || userStore.username }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="main-content">
        <transition name="fade-transform" mode="out-in">
          <router-view :key="$route.fullPath" />
        </transition>
      </el-main>

      <!-- 页脚 -->
      <el-footer class="footer">
        <div class="footer-content">
          <span>© 2026 宋家纺织 · 专注功能家纺面料20年</span>
          <span class="footer-links">
            <a href="http://www.sj-tex.com" target="_blank">官网</a>
            <el-divider direction="vertical" />
            <a href="mailto:sales@sj-tex.com">联系我们</a>
          </span>
        </div>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  HomeFilled,
  Goods,
  Menu,
  Picture,
  User,
  ChatDotRound,
  Star,
  DataAnalysis,
  ArrowDown,
  Expand,
  Fold,
  Refresh,
  FullScreen,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 当前路由名称
const currentRoute = computed(() => route.meta?.title || '')

// 切换侧边栏折叠
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 刷新页面
const refreshPage = () => {
  window.location.reload()
}

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  if (command === 'logout') {
    // 退出登录
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {
      // 取消操作
    })
  } else if (command === 'profile') {
    ElMessage.info('个人信息页面开发中')
  } else if (command === 'settings') {
    ElMessage.info('系统设置页面开发中')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100%;
  max-width: 100%;
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg, #1F2937 0%, #111827 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

/* Logo 区域 */
.logo-container {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 0 16px;
}

.logo-full {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 32px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 18px;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.2;
  letter-spacing: 1px;
}

.logo-subtitle {
  font-size: 12px;
  color: #9CA3AF;
  font-weight: 500;
}

.logo-collapsed {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon-small {
  font-size: 28px;
}

.logo-fade-enter-active,
.logo-fade-leave-active {
  transition: opacity 0.3s;
}

.logo-fade-enter-from,
.logo-fade-leave-to {
  opacity: 0;
}

/* 菜单滚动条 */
.menu-scrollbar {
  height: calc(100vh - 70px);
}

/* 菜单样式 */
.sidebar-menu {
  border-right: none;
  padding: 12px 8px;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 224px;
}

.menu-item,
.menu-submenu {
  margin-bottom: 8px;
  border-radius: 10px;
  overflow: hidden;
}

.sidebar-menu .el-menu-item {
  border-radius: 10px;
  margin-bottom: 4px;
  transition: all 0.3s;
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(79, 70, 229, 0.1) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: #FFFFFF !important;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.sidebar-menu .el-sub-menu__title:hover {
  background-color: rgba(79, 70, 229, 0.1) !important;
}

.menu-title {
  font-weight: 500;
  font-size: 14px;
}

/* 主容器样式 */
.main-container {
  background-color: #F9FAFB;
  flex: 1;
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
}

.header {
  background-color: #FFFFFF;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 64px;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #6B7280;
  transition: all 0.3s;
  padding: 8px;
  border-radius: 8px;
}

.collapse-btn:hover {
  color: #4F46E5;
  background-color: #F3F4F6;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  font-size: 20px;
  cursor: pointer;
  color: #6B7280;
  transition: all 0.3s;
  padding: 8px;
  border-radius: 8px;
}

.header-icon:hover {
  color: #4F46E5;
  background-color: #F3F4F6;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 12px;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: #F3F4F6;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #FFFFFF;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.dropdown-icon {
  color: #9CA3AF;
  font-size: 14px;
}

/* 主内容区 */
.main-content {
  padding: 0;
  min-height: calc(100vh - 64px - 60px);
  background-color: #F9FAFB;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* 页脚 */
.footer {
  background-color: #FFFFFF;
  border-top: 1px solid #E5E7EB;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  font-size: 14px;
  color: #6B7280;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-links a {
  color: #6B7280;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #4F46E5;
}
</style>
