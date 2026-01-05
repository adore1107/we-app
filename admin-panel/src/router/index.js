import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    meta: { requiresAuth: true },
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      // 商品管理
      {
        path: '/product/list',
        name: 'ProductList',
        component: () => import('@/views/product/ProductList.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: '/product/add',
        name: 'ProductAdd',
        component: () => import('@/views/product/ProductForm.vue'),
        meta: { title: '添加商品' }
      },
      {
        path: '/product/edit/:id',
        name: 'ProductEdit',
        component: () => import('@/views/product/ProductForm.vue'),
        meta: { title: '编辑商品' }
      },
      {
        path: '/product/view/:id',
        name: 'ProductView',
        component: () => import('@/views/product/ProductView.vue'),
        meta: { title: '商品详情' }
      },
      // 分类管理
      {
        path: '/category/list',
        name: 'CategoryList',
        component: () => import('@/views/category/CategoryList.vue'),
        meta: { title: '分类管理' }
      },
      // 轮播图管理
      {
        path: '/banner/list',
        name: 'BannerList',
        component: () => import('@/views/banner/BannerList.vue'),
        meta: { title: '轮播图管理' }
      },
      // 评论管理
      {
        path: '/comment/list',
        name: 'CommentList',
        component: () => import('@/views/comment/CommentList.vue'),
        meta: { title: '评论管理' }
      },
      // 收藏管理
      {
        path: '/favorite/list',
        name: 'FavoriteList',
        component: () => import('@/views/favorite/FavoriteList.vue'),
        meta: { title: '收藏管理' }
      },
      // 用户管理
      {
        path: '/user/list',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理' }
      },
      // 数据分析
      {
        path: '/analytics',
        name: 'Analytics',
        component: () => import('@/views/Analytics.vue'),
        meta: { title: '数据分析' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 如果需要登录认证
  if (to.meta.requiresAuth !== false) {
    if (!userStore.isLoggedIn) {
      // 未登录，跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath } // 保存要跳转的页面
      })
    } else {
      next()
    }
  } else {
    // 不需要认证的页面（如登录页）
    if (to.path === '/login' && userStore.isLoggedIn) {
      // 已登录用户访问登录页，跳转到首页
      next({ path: '/' })
    } else {
      next()
    }
  }
})

export default router
