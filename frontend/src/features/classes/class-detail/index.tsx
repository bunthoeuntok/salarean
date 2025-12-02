import { lazy, Suspense } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { ArrowLeft, Users, Calendar, ClipboardList, GraduationCap } from 'lucide-react'
import { Route, type ClassDetailTab } from '@/routes/_authenticated/classes.$id'
import { useLanguage } from '@/context/language-provider'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { classService } from '@/services/class.service'
import { ClassHeader } from './components/class-header'
import { ComingSoonTab } from '../components/coming-soon-tab'
import { ClassDetailErrorBoundary } from '../components/class-detail-error-boundary'
import { ErrorState } from '../components/error-state'

const StudentsTab = lazy(() =>
  import('./components/students-tab').then((module) => ({
    default: module.StudentsTab,
  }))
)

function TabLoadingFallback() {
  return (
    <div className="space-y-2">
      {Array.from({ length: 5 }).map((_, i) => (
        <Skeleton key={i} className="h-16 w-full" />
      ))}
    </div>
  )
}

function ClassDetailContent() {
  const { t } = useLanguage()
  const navigate = useNavigate()
  const { id } = Route.useParams()
  const { tab } = Route.useSearch()

  // Fetch class details
  const {
    data: classData,
    isLoading: isClassLoading,
    error: classError,
    refetch,
  } = useQuery({
    queryKey: ['class', id],
    queryFn: () => classService.getClass(id),
    enabled: !!id,
    retry: (failureCount, error) => {
      // Don't retry on 404 (CLASS_NOT_FOUND)
      if (error && typeof error === 'object' && 'response' in error) {
        const axiosError = error as { response?: { status?: number } }
        if (axiosError.response?.status === 404) {
          return false
        }
      }
      return failureCount < 2
    },
  })

  const handleBack = () => {
    navigate({ to: '/classes' })
  }

  const handleTabChange = (value: string) => {
    navigate({
      to: '/classes/$id',
      params: { id },
      search: { tab: value as ClassDetailTab },
      replace: true,
      resetScroll: false, // Keep scroll position when switching tabs
    })
  }

  // Handle CLASS_NOT_FOUND error
  if (classError) {
    const is404 = classError && typeof classError === 'object' && 'response' in classError
      && (classError as { response?: { status?: number } }).response?.status === 404

    return (
      <>
        <Header fixed />
        <Main>
          <div className="mb-4">
            <Button variant="ghost" size="sm" onClick={handleBack}>
              <ArrowLeft className="mr-2 h-4 w-4" />
              {t.common.back}
            </Button>
          </div>
          <ErrorState
            title={is404 ? 'Class Not Found' : t.common.error}
            message={is404
              ? 'The class you are looking for does not exist or has been removed.'
              : 'Failed to load class details. Please try again.'}
            onRetry={is404 ? undefined : () => refetch()}
          />
        </Main>
      </>
    )
  }

  return (
    <>
      <Header fixed />
      <Main>
        {/* Back Navigation */}
        <div className="mb-4">
          <Button variant="ghost" size="sm" onClick={handleBack}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            {t.common.back}
          </Button>
        </div>

        {/* Class Header */}
        {isClassLoading ? (
          <div className="mb-6 space-y-2">
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-48" />
          </div>
        ) : classData ? (
          <ClassHeader classData={classData} />
        ) : null}

        {/* Tabs Navigation - Mobile responsive with horizontal scroll */}
        <Tabs
          value={tab}
          onValueChange={handleTabChange}
          className="mt-6"
        >
          <div className="-mx-4 overflow-x-auto px-4 sm:mx-0 sm:px-0">
            <TabsList
              className="inline-flex w-auto min-w-full sm:grid sm:w-full sm:grid-cols-4"
              aria-label={t.classes.tabs?.ariaLabel ?? 'Class information tabs'}
            >
              <TabsTrigger
                value="students"
                className="flex shrink-0 items-center gap-2 px-4 sm:px-2"
                aria-controls="tabpanel-students"
              >
                <Users className="h-4 w-4" />
                <span className="hidden sm:inline">{t.classes.tabs?.students ?? 'Students'}</span>
              </TabsTrigger>
              <TabsTrigger
                value="schedule"
                className="flex shrink-0 items-center gap-2 px-4 sm:px-2"
                aria-controls="tabpanel-schedule"
              >
                <Calendar className="h-4 w-4" />
                <span className="hidden sm:inline">{t.classes.tabs?.schedule ?? 'Schedule'}</span>
              </TabsTrigger>
              <TabsTrigger
                value="attendance"
                className="flex shrink-0 items-center gap-2 px-4 sm:px-2"
                aria-controls="tabpanel-attendance"
              >
                <ClipboardList className="h-4 w-4" />
                <span className="hidden sm:inline">{t.classes.tabs?.attendance ?? 'Attendance'}</span>
              </TabsTrigger>
              <TabsTrigger
                value="grades"
                className="flex shrink-0 items-center gap-2 px-4 sm:px-2"
                aria-controls="tabpanel-grades"
              >
                <GraduationCap className="h-4 w-4" />
                <span className="hidden sm:inline">{t.classes.tabs?.grades ?? 'Grades'}</span>
              </TabsTrigger>
            </TabsList>
          </div>

          <TabsContent
            value="students"
            id="tabpanel-students"
            role="tabpanel"
            aria-labelledby="tab-students"
            className="mt-4"
          >
            <Suspense fallback={<TabLoadingFallback />}>
              <StudentsTab classId={id} />
            </Suspense>
          </TabsContent>

          <TabsContent
            value="schedule"
            id="tabpanel-schedule"
            role="tabpanel"
            aria-labelledby="tab-schedule"
            className="mt-4"
          >
            <ComingSoonTab featureName={t.classes.tabs?.schedule ?? 'Schedule'} />
            <p>Lorem ipsum dolor, sit amet consectetur adipisicing elit. Quidem, numquam alias provident quod dolor obcaecati perspiciatis nostrum nisi, rem reprehenderit necessitatibus vitae magni natus! Ullam, eius cumque? Quos vitae maiores dolore, ex facilis debitis aspernatur nulla sit dicta rerum. Illum, explicabo consectetur eligendi dolore porro quibusdam aperiam itaque nesciunt mollitia quis maxime. Similique id expedita ullam aspernatur at veniam iusto. Modi cumque pariatur laudantium commodi fuga, cum earum numquam laboriosam labore neque nihil exercitationem enim, quidem iure quis ducimus et magni eveniet voluptas dolor illo architecto. Nobis, placeat voluptatum harum nihil, quo nulla odit distinctio in saepe, eaque hic. Deleniti dicta sunt quaerat, nulla distinctio unde incidunt itaque. Laborum recusandae necessitatibus consequuntur vero amet consectetur quibusdam harum. Ut quae architecto perspiciatis ad illo at hic fugit placeat sunt, voluptatum dolor magnam tenetur aspernatur corporis. Quis voluptatibus iure velit! Id tenetur at sed exercitationem, ea rem error atque rerum excepturi illum, esse, eveniet voluptate iste illo sapiente repellat dolor dignissimos dicta molestias. Exercitationem cupiditate unde eum vero pariatur inventore assumenda, debitis, quasi omnis aspernatur, illo placeat. Molestiae quos quae repudiandae, animi ducimus quisquam sapiente sequi eos. Minima veniam ut dolorem pariatur aliquam, neque provident temporibus doloribus facilis, fugiat, officia adipisci voluptate eius beatae eveniet? Eaque veritatis minima ullam quas? Numquam rerum ducimus enim soluta optio nam nostrum ea voluptatum quas quod nihil nisi ipsam, iusto eos quaerat perspiciatis laborum neque? Maiores illo voluptates sapiente sed neque repellat, dolores qui incidunt in placeat error, laborum earum aperiam perferendis alias sunt? Id, maiores? Sapiente voluptas voluptates aspernatur excepturi eligendi corporis odio eius totam tempore maiores a sit perspiciatis, cupiditate enim obcaecati autem dolor? Molestias saepe, provident atque impedit minima repellendus at quidem qui quaerat quis, culpa vel voluptatem dolor molestiae veritatis cum! Explicabo, autem similique. Sequi nihil commodi quae nisi voluptatibus provident rem dolorum saepe! Animi provident quo tempore earum autem, ipsum magni accusantium odio, error quidem cupiditate dolore, esse veritatis. Aspernatur voluptates nesciunt alias quam, harum, voluptatem commodi et nemo odit quae atque est beatae, repudiandae enim delectus illo illum quod. Vero culpa eius doloremque velit quibusdam obcaecati possimus dicta ipsum tempore optio exercitationem sunt itaque quod laboriosam temporibus neque placeat quisquam soluta, blanditiis voluptatibus! Omnis amet voluptates, dicta voluptas ullam recusandae, incidunt molestiae error natus ex debitis expedita quidem voluptatem accusantium quis ipsum quam. Temporibus consequatur ullam hic natus nulla cumque, rem, nesciunt id sint quidem quis soluta ab quo, dolorum esse incidunt cum ex quaerat maiores perspiciatis. Enim voluptas quis aut accusamus repellendus necessitatibus, blanditiis sapiente magnam consectetur consequuntur dolorum odit quo quasi minima asperiores veniam ea expedita nesciunt eius maxime, incidunt, illum quia cupiditate assumenda. Delectus laboriosam fugiat qui rerum labore. Enim repellat amet totam libero earum corporis dolor deserunt consectetur, molestias eius reiciendis nisi ea aliquid perspiciatis! Aliquid facere ullam itaque, voluptatum repellat nisi eveniet! At quia accusantium similique explicabo eaque fuga earum odio nisi consequatur, quaerat officia nemo possimus et ex, rem accusamus. Voluptate veniam molestiae quaerat aspernatur voluptas ipsa sequi nisi pariatur sint corporis at placeat eaque quis aut laborum, odit et. Aspernatur voluptas, a numquam dolorum nobis quam error sequi, doloribus culpa deleniti nesciunt rerum aut animi! Omnis iste quis nisi veritatis a! Tenetur eveniet vitae eos omnis reiciendis architecto. Alias vitae sunt nam sint dicta dolorem, nesciunt repellendus tempore harum ullam vel provident consectetur animi, adipisci dolor officiis, modi omnis. Temporibus, cum illo corrupti adipisci fugiat distinctio veritatis est officiis impedit illum sit dicta ab soluta in dolor delectus quidem facilis voluptatibus labore vero deserunt, modi saepe. Neque, temporibus vero. Natus eum enim quo molestias. Quod possimus accusantium cumque veritatis impedit itaque architecto dignissimos modi libero delectus illo inventore illum tempora maxime accusamus esse voluptas deserunt mollitia, eum ea. Quam accusantium mollitia, voluptatem ipsam expedita maiores eum! Expedita voluptas amet doloribus laborum magni illum aliquam eius ullam sint repellendus, omnis, quis modi quibusdam aliquid, voluptatum similique eveniet praesentium! Aliquam nulla voluptatum sint dolorem expedita! Numquam voluptatibus laborum ratione, laudantium odit sequi deleniti similique aliquid accusamus, maxime minus asperiores delectus esse a doloribus, voluptate animi id adipisci modi omnis? Quibusdam nisi ea vitae culpa ipsam architecto debitis itaque doloremque, esse, dignissimos blanditiis. Adipisci, minima excepturi? Quaerat sed porro voluptatum laboriosam numquam? Quam adipisci sint aliquid ab culpa nostrum, ipsam, similique rem laudantium ducimus laborum ipsum doloribus aut corporis vero assumenda eaque repellendus deleniti ipsa quos omnis vitae qui. Recusandae quam sequi molestiae voluptas, aperiam incidunt modi itaque optio. Amet voluptate dolores rerum odio recusandae quasi provident quidem accusantium nesciunt vel eum labore praesentium dolor officiis pariatur cupiditate animi necessitatibus, optio reprehenderit excepturi dignissimos autem. Deleniti non omnis expedita velit? Iste dolorem ullam dolores esse optio ab voluptatum, laudantium ratione ipsam autem quod nihil eius, totam nesciunt voluptatem veritatis asperiores omnis rem debitis. Repellendus eos aliquid dolore natus aut commodi cum fuga, doloremque ex deleniti iste quas, quibusdam alias ad! Cupiditate similique enim, voluptatum ipsum ab numquam, qui iste saepe pariatur quod fuga ad dolorum omnis sint, minus itaque necessitatibus? Explicabo quam id esse saepe reprehenderit fugiat rem earum delectus repudiandae tenetur, eligendi pariatur deserunt aliquam repellendus nihil aut eos nulla adipisci, eveniet itaque quod. Est, veritatis asperiores voluptatum tempore voluptas architecto quibusdam quis, eius labore libero cumque atque laborum in sunt odio! Accusamus numquam molestias alias dolorem debitis maiores cumque perferendis doloremque praesentium natus, vitae iure optio! Ut recusandae, perferendis accusamus atque iusto culpa corrupti quasi maiores reiciendis nemo nihil doloribus animi sequi minima reprehenderit non commodi, suscipit dolor repudiandae nobis beatae aperiam illo! Dignissimos, ratione laudantium! Quo eos quibusdam placeat aliquid laboriosam officiis illum delectus magnam a, et dolores dolorum sunt! Cupiditate tenetur minus eveniet incidunt nemo error molestias, nostrum praesentium optio, perferendis officiis dignissimos saepe. Debitis facere mollitia error maiores? At quod harum dolor iste nisi voluptates quaerat expedita asperiores nostrum quis maxime laudantium voluptatibus, nihil voluptas commodi dolore accusamus incidunt assumenda dolores recusandae voluptatem! Similique, ad aspernatur eveniet ut in consequatur non ab cupiditate corporis exercitationem laudantium mollitia nostrum, eius fugit iure porro placeat, laboriosam aliquam dolorem voluptates numquam dignissimos necessitatibus nulla? Lorem ipsum dolor sit amet consectetur adipisicing elit. Eum neque fuga aperiam, facere quis voluptas excepturi corporis qui quisquam, amet eos quod obcaecati, illum veniam beatae consequuntur odio at ea. Lorem ipsum dolor sit amet consectetur adipisicing elit. Magni corrupti suscipit corporis! Nobis consequatur, rem enim accusantium similique doloremque tempora eligendi dolorem. Inventore debitis similique doloribus, perferendis laboriosam nemo totam veritatis aspernatur voluptates asperiores quo. Voluptate, exercitationem totam! Ratione perspiciatis earum culpa asperiores explicabo, modi reprehenderit nulla voluptatum harum perferendis ea facilis aliquid vel molestias ipsam error cum repudiandae eius quas. Esse, veritatis, nisi magni deleniti rem dolore saepe eos sapiente debitis exercitationem excepturi architecto odit voluptatum voluptatibus corrupti molestiae consequuntur modi facilis et libero! Distinctio quod consequatur ratione porro ex deleniti dolores facere laboriosam, eius provident eveniet magnam nihil voluptate, ipsum officia inventore consectetur quam? Adipisci consectetur incidunt ab quaerat nobis quis vel mollitia cupiditate repudiandae ducimus eius molestias, porro nisi natus recusandae officiis assumenda quibusdam autem expedita vitae neque. Voluptatem beatae inventore modi error sed nemo expedita itaque nobis minus minima, quidem natus? Hic natus, reprehenderit sunt nemo aliquid nobis ipsum. Tempora vero labore repellendus perferendis deleniti. Ipsam quisquam facilis iste nam, voluptates labore eveniet commodi voluptas porro laudantium cum? Mollitia, ab, corporis ratione laboriosam deleniti itaque dolorum beatae perferendis eos consequatur fuga molestiae. Facere sint explicabo sunt quam rerum vero nobis, voluptatum dolore quasi inventore neque deleniti sed maiores magnam odit, necessitatibus beatae exercitationem aspernatur incidunt veritatis? Cumque, ipsa voluptas reiciendis corporis aliquam nihil facere quis, error doloribus inventore magnam eveniet ab dolorum consequuntur? Dicta consectetur deleniti ex? Blanditiis, dicta ut. Inventore repudiandae optio laboriosam adipisci ipsa placeat quae. Libero ipsam at distinctio ratione aliquam ut eveniet ex nesciunt corrupti officia dignissimos, dicta ducimus, iure voluptatum fuga veritatis. Nihil, quod suscipit assumenda iure beatae consequatur ipsa consequuntur rerum doloribus temporibus esse a dolorem repudiandae non saepe animi placeat molestias sequi reprehenderit. Possimus, eligendi excepturi harum iusto delectus unde adipisci ipsa saepe veniam maxime dolorem doloribus explicabo impedit reiciendis corrupti, ipsam architecto? Ab quibusdam consectetur in minus esse hic dignissimos cum, voluptatem quod ullam consequatur, veniam tenetur vero quas omnis, necessitatibus soluta fuga quisquam. Molestias officia repellat fuga provident est, quas iusto illum, facere cumque, laborum animi dignissimos ut tenetur itaque magnam eos error quae! Porro, libero, quaerat sequi facere obcaecati, ab officia autem ex vel atque voluptas deleniti inventore iusto dolor reiciendis quo doloremque velit! Molestias magnam, voluptas dolorum numquam illum fugiat quasi architecto distinctio dolor labore nisi totam accusamus impedit obcaecati omnis eum unde! Explicabo illum dicta fugit ab quia, eos molestiae harum quos? Quia veritatis cupiditate, distinctio vero suscipit, quaerat non ratione id placeat illo ipsa recusandae, animi voluptatum. Placeat sed magni quas explicabo quia, perferendis quo expedita exercitationem voluptate enim id aperiam eveniet molestias praesentium mollitia quam sit minima, nam beatae quae? Quasi rem doloremque adipisci. Vel dicta voluptates pariatur ea voluptatibus quibusdam fugiat quaerat accusantium facere, quos modi amet ut culpa, minus minima doloribus? Inventore sunt hic aspernatur eum molestias minus aliquam veritatis illo minima aut nostrum ut reiciendis quidem dolorum architecto voluptatem doloribus ducimus, quasi, porro provident delectus distinctio sit, quis eos. Vel odit sint velit dolores a porro, molestias autem neque facilis at rerum cumque id magnam incidunt adipisci? Laudantium culpa est at eos molestiae, blanditiis cupiditate earum amet possimus dolores sit obcaecati incidunt tempora iste optio! Incidunt, vel porro! Quibusdam in, dolorem sit quisquam totam ratione cum, minus quae officiis aspernatur repudiandae deleniti saepe. Reprehenderit incidunt distinctio explicabo facilis excepturi amet veniam repellat mollitia ratione maxime, inventore doloremque, neque aut minima. Tempore atque incidunt consequatur? Quis nostrum excepturi mollitia voluptatem vero quam, ducimus cupiditate est eaque quaerat tempore ipsum quo earum ea nam deleniti veritatis consequuntur aliquid? Ad cupiditate, corrupti ex ratione vero pariatur magni quia nam, omnis, earum qui totam impedit dignissimos perspiciatis culpa nemo temporibus excepturi dolore aliquam. Optio modi officia iste cupiditate minus rem tenetur! Architecto quas magni doloribus corrupti voluptatibus soluta iste eum ullam et nemo nesciunt amet, totam voluptatum harum ipsa dignissimos pariatur numquam commodi labore, ex dolor? Ab totam soluta provident sunt quibusdam maxime, omnis enim, excepturi, molestiae repellat accusamus! Voluptatibus sunt numquam voluptates suscipit nulla ratione distinctio aperiam ipsam, eum placeat. Unde, exercitationem, adipisci itaque asperiores praesentium repellat, incidunt accusamus ratione laboriosam perferendis quia non eos consequuntur. Hic optio minima nisi atque quae qui, consectetur reiciendis cum corrupti vel! Repellendus inventore distinctio quas asperiores iste nihil cum corporis ratione, hic dolorem esse quia aliquam ipsam accusantium nesciunt culpa suscipit eos? Nemo odio dolorum veritatis hic tempora consequuntur recusandae vel a ut, ipsum provident dicta nesciunt autem reiciendis minus numquam, alias eveniet neque mollitia aperiam. Eaque asperiores iste ipsam consectetur quibusdam quis sunt facere? Veniam atque, ullam excepturi culpa modi quos. Quis incidunt deserunt, consectetur maiores magnam cupiditate dignissimos perferendis nihil doloribus nulla eveniet dolorum aliquam sequi minus aliquid fugit dolore ipsum enim voluptatibus atque ut eligendi tempore. Doloremque nam odit, cupiditate impedit dicta iure nobis porro repellat itaque iste officia quo possimus at inventore aliquam neque quos, quod a. Accusamus ullam rem commodi sint necessitatibus impedit, nobis ea temporibus nihil dicta numquam, blanditiis ducimus ut culpa. Veritatis, dicta, obcaecati consequuntur blanditiis, accusamus ex vitae deserunt quaerat voluptatibus inventore rerum laboriosam? Dolorem dolore porro deserunt dolorum eligendi voluptatem reprehenderit soluta necessitatibus. Architecto blanditiis iste cumque ipsum labore iure autem perspiciatis, molestias maxime eum incidunt libero dolores beatae pariatur nihil, quos tempora earum voluptatum exercitationem cum saepe temporibus. Pariatur maiores molestiae quod in, aspernatur maxime! Necessitatibus aut porro explicabo commodi nulla temporibus delectus saepe at magni consequatur, architecto excepturi soluta molestiae fugiat. Laborum repellat necessitatibus maxime! Illo sint optio quidem eligendi officia fugiat totam, nisi quos est similique. Placeat reiciendis ut assumenda vitae fugit deleniti eos eaque. Consequuntur nemo eaque, maxime ea molestias excepturi error quos. Tempore fuga hic, magni blanditiis velit quis eveniet est laudantium aliquam saepe laborum, ad nisi. Accusantium, ea quod ipsa voluptate tempora deserunt aspernatur ab numquam iure eum et suscipit itaque at fugit voluptas nobis totam molestias optio porro impedit quasi doloribus. Minima eligendi beatae blanditiis voluptatibus omnis temporibus ut tenetur, suscipit perferendis, dicta sapiente laudantium cum ipsum velit fuga pariatur nostrum officiis? Iusto ea nam harum. Labore, ipsam quibusdam.</p>
          </TabsContent>

          <TabsContent
            value="attendance"
            id="tabpanel-attendance"
            role="tabpanel"
            aria-labelledby="tab-attendance"
            className="mt-4"
          >
            <ComingSoonTab featureName={t.classes.tabs?.attendance ?? 'Attendance'} />
          </TabsContent>

          <TabsContent
            value="grades"
            id="tabpanel-grades"
            role="tabpanel"
            aria-labelledby="tab-grades"
            className="mt-4"
          >
            <ComingSoonTab featureName={t.classes.tabs?.grades ?? 'Grades'} />
          </TabsContent>
        </Tabs>
      </Main>
    </>
  )
}

export function ClassDetailPage() {
  const navigate = useNavigate()

  const handleBack = () => {
    navigate({ to: '/classes' })
  }

  return (
    <ClassDetailErrorBoundary onBack={handleBack}>
      <ClassDetailContent />
    </ClassDetailErrorBoundary>
  )
}
