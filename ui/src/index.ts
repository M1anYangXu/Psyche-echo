import { definePlugin } from '@halo-dev/ui-shared'
import { IconPlug } from '@halo-dev/components'
import { markRaw } from 'vue'
import 'remixicon/fonts/remixicon.css'

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: 'Root',
      route: {
        path: '/psyche-echo',
        name: 'PsycheEcho',
        component: () => import(/* webpackChunkName: "HomeView" */ './views/HomeView.vue'),
        meta: {
          title: '心灵回响',
          searchable: true,
          menu: {
            name: '心灵回响',
            group: '心灵回响分组',
            icon: markRaw(IconPlug),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {},
})
